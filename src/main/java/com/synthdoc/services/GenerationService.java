package com.synthdoc.services;

import com.synthdoc.clients.ClaudeClient;
import com.synthdoc.models.Document;
import com.synthdoc.models.GenerationRequest;
import com.synthdoc.models.Section;
import java.time.Instant;
import java.util.Optional;

public class GenerationService {

    private final ClaudeClient claudeClient;
    private final DocumentService documentService;
    private final VersionService versionService;

    public GenerationService(ClaudeClient claudeClient, DocumentService documentService, VersionService versionService) {
        this.claudeClient = claudeClient;
        this.documentService = documentService;
        this.versionService = versionService;
    }

    public Document generateAllSections(String documentId, GenerationRequest request) {
        Document doc = documentService.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        for (Section section : doc.getSections()) {
            String prompt = buildPrompt(doc, section, request);
            String content = claudeClient.generate(prompt, request.getMaxTokens());
            section.setContent(content);
        }

        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(Instant.now());
        versionService.saveVersion(documentId, doc.deepCopy(), "AI generated all sections");
        return doc;
    }

    public Document generateSection(String documentId, String sectionId, GenerationRequest request) {
        Document doc = documentService.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        Section target = doc.getSections().stream()
                .filter(s -> s.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        String prompt = buildPrompt(doc, target, request);
        String content = claudeClient.generate(prompt, request.getMaxTokens());
        target.setContent(content);

        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(Instant.now());
        versionService.saveVersion(documentId, doc.deepCopy(), "AI generated section: " + target.getTitle());
        return doc;
    }

    public String generateContent(String prompt, int maxTokens) {
        if (!claudeClient.isAvailable()) {
            throw new IllegalStateException("Claude client is not available");
        }
        return claudeClient.generate(prompt, maxTokens);
    }

    public boolean isClientAvailable() {
        return claudeClient.isAvailable();
    }

    private String buildPrompt(Document doc, Section section, GenerationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate content for a document section.\n\n");
        sb.append("Document Title: ").append(doc.getTitle()).append("\n");
        sb.append("Section Title: ").append(section.getTitle()).append("\n");
        sb.append("Section Type: ").append(section.getType()).append("\n");

        if (request.getTone() != null) {
            sb.append("Tone: ").append(request.getTone()).append("\n");
        }
        if (request.getContext() != null) {
            sb.append("Additional Context: ").append(request.getContext()).append("\n");
        }

        sb.append("\nPlease generate professional, well-structured content for this ");
        sb.append(section.getType().name().toLowerCase()).append(" section.");

        return sb.toString();
    }
}
