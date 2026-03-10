package com.synthdoc.services;

import com.synthdoc.models.Citation;
import com.synthdoc.models.Citation.CitationFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CitationService {

    private final Map<String, Citation> citations = new ConcurrentHashMap<>();

    public Citation create(Citation citation) {
        if (citation.getDocumentId() == null || citation.getDocumentId().isBlank()) {
            throw new IllegalArgumentException("Document ID is required");
        }
        if (citation.getText() == null || citation.getText().isBlank()) {
            throw new IllegalArgumentException("Citation text is required");
        }
        if (citation.getSource() == null || citation.getSource().isBlank()) {
            throw new IllegalArgumentException("Citation source is required");
        }
        if (citation.getFormat() == null) {
            citation.setFormat(CitationFormat.APA);
        }
        citations.put(citation.getId(), citation);
        return citation;
    }

    public Optional<Citation> findById(String id) {
        return Optional.ofNullable(citations.get(id));
    }

    public List<Citation> findByDocumentId(String documentId) {
        return citations.values().stream()
                .filter(c -> documentId.equals(c.getDocumentId()))
                .collect(Collectors.toList());
    }

    public List<Citation> findBySectionId(String documentId, String sectionId) {
        return citations.values().stream()
                .filter(c -> documentId.equals(c.getDocumentId()) && sectionId.equals(c.getSectionId()))
                .collect(Collectors.toList());
    }

    public List<Citation> findByFormat(String documentId, CitationFormat format) {
        return citations.values().stream()
                .filter(c -> documentId.equals(c.getDocumentId()) && c.getFormat() == format)
                .collect(Collectors.toList());
    }

    public Optional<Citation> update(String id, Citation updated) {
        Citation existing = citations.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (updated.getText() != null && !updated.getText().isBlank()) {
            existing.setText(updated.getText());
        }
        if (updated.getSource() != null && !updated.getSource().isBlank()) {
            existing.setSource(updated.getSource());
        }
        if (updated.getFormat() != null) {
            existing.setFormat(updated.getFormat());
        }
        if (updated.getSectionId() != null) {
            existing.setSectionId(updated.getSectionId());
        }
        if (updated.getMetadata() != null) {
            existing.getMetadata().putAll(updated.getMetadata());
        }
        return Optional.of(existing);
    }

    public boolean delete(String id) {
        return citations.remove(id) != null;
    }

    public int deleteByDocumentId(String documentId) {
        List<String> toRemove = citations.values().stream()
                .filter(c -> documentId.equals(c.getDocumentId()))
                .map(Citation::getId)
                .collect(Collectors.toList());
        toRemove.forEach(citations::remove);
        return toRemove.size();
    }

    public String generateBibliography(String documentId, CitationFormat format) {
        List<Citation> docCitations = findByDocumentId(documentId);
        if (docCitations.isEmpty()) {
            return "No citations found for this document.";
        }

        StringBuilder bib = new StringBuilder();
        bib.append("Bibliography\n");
        bib.append("============\n\n");

        List<Citation> sorted = docCitations.stream()
                .sorted((a, b) -> a.getSource().compareToIgnoreCase(b.getSource()))
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            Citation c = sorted.get(i);
            CitationFormat useFormat = format != null ? format : c.getFormat();
            bib.append(formatCitation(c, useFormat, i + 1));
            bib.append("\n");
        }

        return bib.toString().trim();
    }

    public String generateBibliography(String documentId) {
        return generateBibliography(documentId, null);
    }

    private String formatCitation(Citation citation, CitationFormat format, int index) {
        return switch (format) {
            case APA -> String.format("[%d] %s. %s.\n", index, citation.getSource(), citation.getText());
            case MLA -> String.format("[%d] %s. \"%s.\"\n", index, citation.getSource(), citation.getText());
            case CHICAGO -> String.format("[%d] %s, %s.\n", index, citation.getSource(), citation.getText());
        };
    }

    public int count() {
        return citations.size();
    }

    public int countByDocument(String documentId) {
        return (int) citations.values().stream()
                .filter(c -> documentId.equals(c.getDocumentId()))
                .count();
    }
}
