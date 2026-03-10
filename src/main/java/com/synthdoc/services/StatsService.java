package com.synthdoc.services;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatsService {

    private final TemplateService templateService;
    private final DocumentService documentService;
    private final CitationService citationService;
    private final VersionService versionService;

    public StatsService(TemplateService templateService, DocumentService documentService,
                        CitationService citationService, VersionService versionService) {
        this.templateService = templateService;
        this.documentService = documentService;
        this.citationService = citationService;
        this.versionService = versionService;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTemplates", templateService.count());
        stats.put("totalDocuments", documentService.count());
        stats.put("totalCitations", citationService.count());
        stats.put("totalVersions", versionService.totalVersions());
        stats.put("platform", "SynthDoc");
        stats.put("version", "1.0.0");
        return stats;
    }

    public int getTotalTemplates() {
        return templateService.count();
    }

    public int getTotalDocuments() {
        return documentService.count();
    }

    public int getTotalCitations() {
        return citationService.count();
    }

    public int getTotalVersions() {
        return versionService.totalVersions();
    }
}
