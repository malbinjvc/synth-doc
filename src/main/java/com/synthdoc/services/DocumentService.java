package com.synthdoc.services;

import com.synthdoc.models.Document;
import com.synthdoc.models.Section;
import com.synthdoc.models.Template;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DocumentService {

    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final TemplateService templateService;
    private final VersionService versionService;

    public DocumentService(TemplateService templateService, VersionService versionService) {
        this.templateService = templateService;
        this.versionService = versionService;
    }

    public Document createFromTemplate(String templateId, String title, Map<String, String> metadata) {
        Template template = templateService.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        Document doc = new Document();
        doc.setTemplateId(templateId);
        doc.setTitle(title != null ? title : template.getName() + " Document");
        if (metadata != null) {
            doc.setMetadata(metadata);
        }

        // Copy sections from template
        List<Section> docSections = new ArrayList<>();
        for (Section ts : template.getSections()) {
            Section ds = ts.copy();
            docSections.add(ds);
        }
        doc.setSections(docSections);

        documents.put(doc.getId(), doc);
        versionService.saveVersion(doc.getId(), doc.deepCopy(), "Initial creation from template");
        return doc;
    }

    public Document create(Document document) {
        if (document.getTitle() == null || document.getTitle().isBlank()) {
            throw new IllegalArgumentException("Document title is required");
        }
        documents.put(document.getId(), document);
        versionService.saveVersion(document.getId(), document.deepCopy(), "Initial creation");
        return document;
    }

    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    public List<Document> findAll() {
        return new ArrayList<>(documents.values());
    }

    public List<Document> findByTemplateId(String templateId) {
        return documents.values().stream()
                .filter(d -> templateId.equals(d.getTemplateId()))
                .collect(Collectors.toList());
    }

    public Optional<Document> update(String id, Document updated) {
        Document existing = documents.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (updated.getTitle() != null && !updated.getTitle().isBlank()) {
            existing.setTitle(updated.getTitle());
        }
        if (updated.getMetadata() != null) {
            existing.getMetadata().putAll(updated.getMetadata());
        }
        existing.setVersion(existing.getVersion() + 1);
        existing.setUpdatedAt(Instant.now());

        versionService.saveVersion(id, existing.deepCopy(), "Document updated");
        return Optional.of(existing);
    }

    public boolean delete(String id) {
        return documents.remove(id) != null;
    }

    public Optional<Document> updateSection(String docId, String sectionId, String content) {
        Document doc = documents.get(docId);
        if (doc == null) {
            return Optional.empty();
        }
        for (Section s : doc.getSections()) {
            if (s.getId().equals(sectionId)) {
                s.setContent(content);
                doc.setVersion(doc.getVersion() + 1);
                doc.setUpdatedAt(Instant.now());
                versionService.saveVersion(docId, doc.deepCopy(), "Section '" + s.getTitle() + "' updated");
                return Optional.of(doc);
            }
        }
        throw new IllegalArgumentException("Section not found: " + sectionId);
    }

    public Optional<Section> findSection(String docId, String sectionId) {
        Document doc = documents.get(docId);
        if (doc == null) {
            return Optional.empty();
        }
        return doc.getSections().stream()
                .filter(s -> s.getId().equals(sectionId))
                .findFirst();
    }

    public int count() {
        return documents.size();
    }

    public Optional<Document> rollback(String docId, int version) {
        Document restored = versionService.rollback(docId, version);
        if (restored == null) {
            return Optional.empty();
        }
        restored.setVersion(documents.get(docId).getVersion() + 1);
        restored.setUpdatedAt(Instant.now());
        documents.put(docId, restored);
        versionService.saveVersion(docId, restored.deepCopy(), "Rolled back to version " + version);
        return Optional.of(restored);
    }
}
