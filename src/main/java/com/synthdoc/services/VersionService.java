package com.synthdoc.services;

import com.synthdoc.models.Document;
import com.synthdoc.models.DocumentVersion;
import com.synthdoc.models.Section;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VersionService {

    private final Map<String, List<DocumentVersion>> versionHistory = new ConcurrentHashMap<>();

    public void saveVersion(String documentId, Document snapshot, String description) {
        List<DocumentVersion> versions = versionHistory.computeIfAbsent(documentId, k -> new ArrayList<>());
        int nextVersion = versions.size() + 1;
        DocumentVersion version = new DocumentVersion(nextVersion, snapshot, description);
        versions.add(version);
    }

    public List<DocumentVersion> getVersionHistory(String documentId) {
        return versionHistory.getOrDefault(documentId, new ArrayList<>());
    }

    public DocumentVersion getVersion(String documentId, int versionNumber) {
        List<DocumentVersion> versions = versionHistory.get(documentId);
        if (versions == null) {
            return null;
        }
        return versions.stream()
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst()
                .orElse(null);
    }

    public Document rollback(String documentId, int versionNumber) {
        DocumentVersion version = getVersion(documentId, versionNumber);
        if (version == null) {
            return null;
        }
        return version.getSnapshot().deepCopy();
    }

    public String diff(String documentId, int version1, int version2) {
        DocumentVersion v1 = getVersion(documentId, version1);
        DocumentVersion v2 = getVersion(documentId, version2);

        if (v1 == null || v2 == null) {
            throw new IllegalArgumentException("One or both versions not found");
        }

        StringBuilder diff = new StringBuilder();
        diff.append("Diff between version ").append(version1).append(" and version ").append(version2).append("\n");
        diff.append("=".repeat(60)).append("\n\n");

        Document d1 = v1.getSnapshot();
        Document d2 = v2.getSnapshot();

        // Title diff
        if (!d1.getTitle().equals(d2.getTitle())) {
            diff.append("Title changed:\n");
            diff.append("  - ").append(d1.getTitle()).append("\n");
            diff.append("  + ").append(d2.getTitle()).append("\n\n");
        }

        // Section diffs
        for (Section s2 : d2.getSections()) {
            Section s1 = d1.getSections().stream()
                    .filter(s -> s.getId().equals(s2.getId()))
                    .findFirst()
                    .orElse(null);

            if (s1 == null) {
                diff.append("Added section: ").append(s2.getTitle()).append("\n");
                diff.append("  + ").append(truncate(s2.getContent(), 100)).append("\n\n");
            } else {
                String c1 = s1.getContent() != null ? s1.getContent() : "";
                String c2 = s2.getContent() != null ? s2.getContent() : "";
                if (!c1.equals(c2)) {
                    diff.append("Modified section: ").append(s2.getTitle()).append("\n");
                    diff.append("  - ").append(truncate(c1, 100)).append("\n");
                    diff.append("  + ").append(truncate(c2, 100)).append("\n\n");
                }
            }
        }

        // Check for removed sections
        for (Section s1 : d1.getSections()) {
            boolean exists = d2.getSections().stream().anyMatch(s -> s.getId().equals(s1.getId()));
            if (!exists) {
                diff.append("Removed section: ").append(s1.getTitle()).append("\n\n");
            }
        }

        return diff.toString().trim();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "(empty)";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }

    public int getLatestVersionNumber(String documentId) {
        List<DocumentVersion> versions = versionHistory.get(documentId);
        if (versions == null || versions.isEmpty()) {
            return 0;
        }
        return versions.size();
    }

    public int totalVersions() {
        return versionHistory.values().stream().mapToInt(List::size).sum();
    }

    public void clearHistory(String documentId) {
        versionHistory.remove(documentId);
    }
}
