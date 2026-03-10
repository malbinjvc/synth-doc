package com.synthdoc;

import com.synthdoc.models.Citation;
import com.synthdoc.models.Document;
import com.synthdoc.models.Section;
import com.synthdoc.models.Template;
import com.synthdoc.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {

    private ExportService exportService;
    private DocumentService documentService;
    private CitationService citationService;
    private Document testDoc;

    @BeforeEach
    void setUp() {
        TemplateService templateService = new TemplateService();
        VersionService versionService = new VersionService();
        citationService = new CitationService();
        documentService = new DocumentService(templateService, versionService);
        exportService = new ExportService(documentService, citationService);

        Section s1 = new Section(Section.SectionType.INTRODUCTION, "Introduction", "This is the intro.", 1);
        Section s2 = new Section(Section.SectionType.BODY, "Analysis", "Main analysis here.", 2);
        Section s3 = new Section(Section.SectionType.CONCLUSION, "Conclusion", "Final thoughts.", 3);
        Template template = new Template("Export Test", "desc", Template.TemplateType.REPORT,
                List.of(s1, s2, s3), null);
        templateService.create(template);
        testDoc = documentService.createFromTemplate(template.getId(), "AI Research Report",
                Map.of("author", "Jane Doe", "department", "Engineering"));
    }

    @Test
    void exportAsJson_returnsValidJson() {
        String json = exportService.exportAsJson(testDoc.getId());
        assertNotNull(json);
        assertTrue(json.contains("AI Research Report"));
        assertTrue(json.contains("Introduction"));
        assertTrue(json.contains("Analysis"));
    }

    @Test
    void exportAsJson_containsSections() {
        String json = exportService.exportAsJson(testDoc.getId());
        assertTrue(json.contains("sections"));
        assertTrue(json.contains("Introduction"));
    }

    @Test
    void exportAsText_returnsFormattedText() {
        String text = exportService.exportAsText(testDoc.getId());
        assertNotNull(text);
        assertTrue(text.contains("AI RESEARCH REPORT"));
        assertTrue(text.contains("Introduction"));
        assertTrue(text.contains("Analysis"));
        assertTrue(text.contains("Conclusion"));
    }

    @Test
    void exportAsText_includesMetadata() {
        String text = exportService.exportAsText(testDoc.getId());
        assertTrue(text.contains("author"));
        assertTrue(text.contains("Jane Doe"));
    }

    @Test
    void exportAsText_includesCitations() {
        citationService.create(new Citation(testDoc.getId(), "sec1",
                "AI advances", "Smith, J.", Citation.CitationFormat.APA, null));
        String text = exportService.exportAsText(testDoc.getId());
        assertTrue(text.contains("REFERENCES"));
        assertTrue(text.contains("Smith"));
    }

    @Test
    void exportAsMarkdown_returnsFormattedMarkdown() {
        String md = exportService.exportAsMarkdown(testDoc.getId());
        assertNotNull(md);
        assertTrue(md.startsWith("# AI Research Report"));
        assertTrue(md.contains("## Introduction"));
        assertTrue(md.contains("## Analysis"));
        assertTrue(md.contains("## Conclusion"));
    }

    @Test
    void exportAsMarkdown_includesVersion() {
        String md = exportService.exportAsMarkdown(testDoc.getId());
        assertTrue(md.contains("Version 1"));
    }

    @Test
    void exportAsMarkdown_includesCitations() {
        citationService.create(new Citation(testDoc.getId(), "sec1",
                "AI advances", "Smith, J.", Citation.CitationFormat.APA, null));
        String md = exportService.exportAsMarkdown(testDoc.getId());
        assertTrue(md.contains("## References"));
        assertTrue(md.contains("Smith"));
    }

    @Test
    void export_jsonFormat_returnsJson() {
        String result = exportService.export(testDoc.getId(), "json");
        assertTrue(result.contains("\"title\""));
    }

    @Test
    void export_textFormat_returnsText() {
        String result = exportService.export(testDoc.getId(), "text");
        assertTrue(result.contains("AI RESEARCH REPORT"));
    }

    @Test
    void export_markdownFormat_returnsMarkdown() {
        String result = exportService.export(testDoc.getId(), "markdown");
        assertTrue(result.startsWith("# "));
    }

    @Test
    void export_unsupportedFormat_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> exportService.export(testDoc.getId(), "pdf"));
    }

    @Test
    void export_invalidDocId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> exportService.export("nonexistent", "json"));
    }

    @Test
    void isFormatSupported_validFormats_returnsTrue() {
        assertTrue(exportService.isFormatSupported("json"));
        assertTrue(exportService.isFormatSupported("text"));
        assertTrue(exportService.isFormatSupported("txt"));
        assertTrue(exportService.isFormatSupported("markdown"));
        assertTrue(exportService.isFormatSupported("md"));
    }

    @Test
    void isFormatSupported_invalidFormat_returnsFalse() {
        assertFalse(exportService.isFormatSupported("pdf"));
        assertFalse(exportService.isFormatSupported("html"));
        assertFalse(exportService.isFormatSupported(null));
    }

    @Test
    void exportAsText_emptySections_showsPlaceholder() {
        Document doc = new Document();
        doc.setTitle("Empty Doc");
        Section emptySection = new Section(Section.SectionType.BODY, "Empty", "", 1);
        doc.setSections(List.of(emptySection));
        documentService.create(doc);

        String text = exportService.exportAsText(doc.getId());
        assertTrue(text.contains("[No content generated yet]"));
    }
}
