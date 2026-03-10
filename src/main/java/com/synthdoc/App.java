package com.synthdoc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.synthdoc.clients.ClaudeClient;
import com.synthdoc.clients.MockClaudeClient;
import com.synthdoc.models.*;
import com.synthdoc.services.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinJackson;

import java.util.List;
import java.util.Map;

public class App {

    private final Javalin app;
    private final TemplateService templateService;
    private final DocumentService documentService;
    private final GenerationService generationService;
    private final CitationService citationService;
    private final VersionService versionService;
    private final ExportService exportService;
    private final StatsService statsService;

    public App(ClaudeClient claudeClient) {
        this.templateService = new TemplateService();
        this.versionService = new VersionService();
        this.citationService = new CitationService();
        this.documentService = new DocumentService(templateService, versionService);
        this.generationService = new GenerationService(claudeClient, documentService, versionService);
        this.exportService = new ExportService(documentService, citationService);
        this.statsService = new StatsService(templateService, documentService, citationService, versionService);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, true));
        });

        registerRoutes();
    }

    private void registerRoutes() {
        // Health
        app.get("/health", ctx -> ctx.json(Map.of(
                "status", "healthy",
                "service", "SynthDoc",
                "version", "1.0.0"
        )));

        // Template endpoints
        app.post("/api/templates", this::createTemplate);
        app.get("/api/templates", this::listTemplates);
        app.get("/api/templates/{id}", this::getTemplate);
        app.put("/api/templates/{id}", this::updateTemplate);
        app.delete("/api/templates/{id}", this::deleteTemplate);

        // Document endpoints
        app.post("/api/documents", this::createDocument);
        app.get("/api/documents", this::listDocuments);
        app.get("/api/documents/{id}", this::getDocument);

        // Generation endpoints
        app.post("/api/documents/{id}/generate", this::generateAllSections);
        app.post("/api/documents/{id}/sections/{sectionId}/generate", this::generateSection);

        // Version endpoints
        app.get("/api/documents/{id}/versions", this::getVersions);
        app.post("/api/documents/{id}/rollback/{version}", this::rollbackDocument);

        // Export endpoints
        app.get("/api/documents/{id}/export", this::exportDocument);

        // Citation endpoints
        app.post("/api/citations", this::createCitation);
        app.get("/api/documents/{id}/citations", this::getDocumentCitations);
        app.get("/api/documents/{id}/bibliography", this::getBibliography);

        // Stats
        app.get("/api/stats", this::getStats);

        // Exception handlers
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(Map.of("error", "Internal server error: " + e.getMessage()));
        });
    }

    // Template handlers
    private void createTemplate(Context ctx) {
        Template template = ctx.bodyAsClass(Template.class);
        Template created = templateService.create(template);
        ctx.status(201).json(created);
    }

    private void listTemplates(Context ctx) {
        String type = ctx.queryParam("type");
        List<Template> templates;
        if (type != null) {
            templates = templateService.findByType(Template.TemplateType.valueOf(type.toUpperCase()));
        } else {
            templates = templateService.findAll();
        }
        ctx.json(templates);
    }

    private void getTemplate(Context ctx) {
        String id = ctx.pathParam("id");
        templateService.findById(id)
                .ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(404).json(Map.of("error", "Template not found"))
                );
    }

    private void updateTemplate(Context ctx) {
        String id = ctx.pathParam("id");
        Template updated = ctx.bodyAsClass(Template.class);
        templateService.update(id, updated)
                .ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(404).json(Map.of("error", "Template not found"))
                );
    }

    private void deleteTemplate(Context ctx) {
        String id = ctx.pathParam("id");
        if (templateService.delete(id)) {
            ctx.status(204);
        } else {
            ctx.status(404).json(Map.of("error", "Template not found"));
        }
    }

    // Document handlers
    private void createDocument(Context ctx) {
        var body = ctx.bodyAsClass(Map.class);
        String templateId = (String) body.get("templateId");
        String title = (String) body.get("title");
        @SuppressWarnings("unchecked")
        Map<String, String> metadata = (Map<String, String>) body.get("metadata");

        if (templateId != null) {
            Document doc = documentService.createFromTemplate(templateId, title, metadata);
            ctx.status(201).json(doc);
        } else {
            Document doc = new Document();
            doc.setTitle(title);
            if (metadata != null) {
                doc.setMetadata(metadata);
            }
            Document created = documentService.create(doc);
            ctx.status(201).json(created);
        }
    }

    private void listDocuments(Context ctx) {
        ctx.json(documentService.findAll());
    }

    private void getDocument(Context ctx) {
        String id = ctx.pathParam("id");
        documentService.findById(id)
                .ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(404).json(Map.of("error", "Document not found"))
                );
    }

    // Generation handlers
    private void generateAllSections(Context ctx) {
        String id = ctx.pathParam("id");
        GenerationRequest request = ctx.bodyAsClass(GenerationRequest.class);
        request.setDocumentId(id);
        Document doc = generationService.generateAllSections(id, request);
        ctx.json(doc);
    }

    private void generateSection(Context ctx) {
        String id = ctx.pathParam("id");
        String sectionId = ctx.pathParam("sectionId");
        GenerationRequest request = ctx.bodyAsClass(GenerationRequest.class);
        request.setDocumentId(id);
        request.setSectionId(sectionId);
        Document doc = generationService.generateSection(id, sectionId, request);
        ctx.json(doc);
    }

    // Version handlers
    private void getVersions(Context ctx) {
        String id = ctx.pathParam("id");
        ctx.json(versionService.getVersionHistory(id));
    }

    private void rollbackDocument(Context ctx) {
        String id = ctx.pathParam("id");
        int version = Integer.parseInt(ctx.pathParam("version"));
        documentService.rollback(id, version)
                .ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(404).json(Map.of("error", "Version not found"))
                );
    }

    // Export handlers
    private void exportDocument(Context ctx) {
        String id = ctx.pathParam("id");
        String format = ctx.queryParam("format");
        if (format == null) {
            format = "json";
        }
        String exported = exportService.export(id, format);
        switch (format.toLowerCase()) {
            case "json" -> ctx.contentType("application/json").result(exported);
            case "text", "txt" -> ctx.contentType("text/plain").result(exported);
            case "markdown", "md" -> ctx.contentType("text/markdown").result(exported);
            default -> ctx.status(400).json(Map.of("error", "Unsupported format"));
        }
    }

    // Citation handlers
    private void createCitation(Context ctx) {
        Citation citation = ctx.bodyAsClass(Citation.class);
        Citation created = citationService.create(citation);
        ctx.status(201).json(created);
    }

    private void getDocumentCitations(Context ctx) {
        String id = ctx.pathParam("id");
        ctx.json(citationService.findByDocumentId(id));
    }

    private void getBibliography(Context ctx) {
        String id = ctx.pathParam("id");
        String formatParam = ctx.queryParam("format");
        String bib;
        if (formatParam != null) {
            Citation.CitationFormat format = Citation.CitationFormat.valueOf(formatParam.toUpperCase());
            bib = citationService.generateBibliography(id, format);
        } else {
            bib = citationService.generateBibliography(id);
        }
        ctx.contentType("text/plain").result(bib);
    }

    // Stats handler
    private void getStats(Context ctx) {
        ctx.json(statsService.getStats());
    }

    public Javalin javalinApp() {
        return app;
    }

    public void start(int port) {
        app.start(port);
    }

    public void stop() {
        app.stop();
    }

    // Expose services for testing
    public TemplateService getTemplateService() { return templateService; }
    public DocumentService getDocumentService() { return documentService; }
    public GenerationService getGenerationService() { return generationService; }
    public CitationService getCitationService() { return citationService; }
    public VersionService getVersionService() { return versionService; }
    public ExportService getExportService() { return exportService; }
    public StatsService getStatsService() { return statsService; }

    public static void main(String[] args) {
        String apiKey = System.getenv("CLAUDE_API_KEY");
        ClaudeClient client;
        if (apiKey != null && !apiKey.isEmpty()) {
            client = new com.synthdoc.clients.HttpClaudeClient(apiKey);
        } else {
            System.out.println("No CLAUDE_API_KEY set, using mock client");
            client = new MockClaudeClient();
        }

        App app = new App(client);
        int port = 7070;
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            port = Integer.parseInt(portEnv);
        }
        app.start(port);
        System.out.println("SynthDoc running on port " + port);
    }
}
