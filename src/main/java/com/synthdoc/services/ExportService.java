package com.synthdoc.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.synthdoc.models.Citation;
import com.synthdoc.models.Document;
import com.synthdoc.models.Section;
import java.util.Comparator;
import java.util.List;

public class ExportService {

    private final DocumentService documentService;
    private final CitationService citationService;
    private final ObjectMapper objectMapper;

    public ExportService(DocumentService documentService, CitationService citationService) {
        this.documentService = documentService;
        this.citationService = citationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String exportAsJson(String documentId) {
        Document doc = getDocument(documentId);
        try {
            return objectMapper.writeValueAsString(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export document as JSON", e);
        }
    }

    public String exportAsText(String documentId) {
        Document doc = getDocument(documentId);
        StringBuilder sb = new StringBuilder();

        sb.append(doc.getTitle().toUpperCase()).append("\n");
        sb.append("=".repeat(doc.getTitle().length())).append("\n\n");

        // Metadata
        if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
            for (var entry : doc.getMetadata().entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("\n");
        }

        // Sections ordered
        List<Section> ordered = doc.getSections().stream()
                .sorted(Comparator.comparingInt(Section::getOrder))
                .toList();

        for (Section section : ordered) {
            sb.append(section.getTitle()).append("\n");
            sb.append("-".repeat(section.getTitle().length())).append("\n");
            if (section.getContent() != null && !section.getContent().isBlank()) {
                sb.append(section.getContent()).append("\n");
            } else {
                sb.append("[No content generated yet]").append("\n");
            }
            sb.append("\n");
        }

        // Citations
        List<Citation> citations = citationService.findByDocumentId(documentId);
        if (!citations.isEmpty()) {
            sb.append("REFERENCES\n");
            sb.append("----------\n");
            for (int i = 0; i < citations.size(); i++) {
                Citation c = citations.get(i);
                sb.append("[").append(i + 1).append("] ").append(c.getSource())
                  .append(". ").append(c.getText()).append("\n");
            }
        }

        return sb.toString().trim();
    }

    public String exportAsMarkdown(String documentId) {
        Document doc = getDocument(documentId);
        StringBuilder sb = new StringBuilder();

        sb.append("# ").append(doc.getTitle()).append("\n\n");

        // Metadata
        if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
            sb.append("---\n");
            for (var entry : doc.getMetadata().entrySet()) {
                sb.append("**").append(entry.getKey()).append("**: ").append(entry.getValue()).append("\n\n");
            }
            sb.append("---\n\n");
        }

        // Version info
        sb.append("*Version ").append(doc.getVersion()).append(" | Created: ")
          .append(doc.getCreatedAt()).append("*\n\n");

        // Sections ordered
        List<Section> ordered = doc.getSections().stream()
                .sorted(Comparator.comparingInt(Section::getOrder))
                .toList();

        for (Section section : ordered) {
            sb.append("## ").append(section.getTitle()).append("\n\n");
            if (section.getContent() != null && !section.getContent().isBlank()) {
                sb.append(section.getContent()).append("\n\n");
            } else {
                sb.append("*[No content generated yet]*\n\n");
            }
        }

        // Citations
        List<Citation> citations = citationService.findByDocumentId(documentId);
        if (!citations.isEmpty()) {
            sb.append("## References\n\n");
            for (int i = 0; i < citations.size(); i++) {
                Citation c = citations.get(i);
                sb.append(i + 1).append(". ").append(c.getSource())
                  .append(". *").append(c.getText()).append("*\n");
            }
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    public String export(String documentId, String format) {
        return switch (format.toLowerCase()) {
            case "json" -> exportAsJson(documentId);
            case "text", "txt" -> exportAsText(documentId);
            case "markdown", "md" -> exportAsMarkdown(documentId);
            default -> throw new IllegalArgumentException("Unsupported export format: " + format);
        };
    }

    public boolean isFormatSupported(String format) {
        return format != null && List.of("json", "text", "txt", "markdown", "md")
                .contains(format.toLowerCase());
    }

    private Document getDocument(String documentId) {
        return documentService.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }
}
