package com.synthdoc;

import com.synthdoc.models.Document;
import com.synthdoc.models.Section;
import com.synthdoc.models.Template;
import com.synthdoc.services.DocumentService;
import com.synthdoc.services.TemplateService;
import com.synthdoc.services.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {

    private DocumentService documentService;
    private TemplateService templateService;
    private VersionService versionService;
    private Template testTemplate;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService();
        versionService = new VersionService();
        documentService = new DocumentService(templateService, versionService);

        Section s1 = new Section(Section.SectionType.INTRODUCTION, "Introduction", "", 1);
        Section s2 = new Section(Section.SectionType.BODY, "Body", "", 2);
        Section s3 = new Section(Section.SectionType.CONCLUSION, "Conclusion", "", 3);
        testTemplate = new Template("Test Template", "A test template",
                Template.TemplateType.REPORT, List.of(s1, s2, s3), null);
        templateService.create(testTemplate);
    }

    @Test
    void createFromTemplate_validTemplate_createsDocument() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "My Report", null);
        assertNotNull(doc.getId());
        assertEquals("My Report", doc.getTitle());
        assertEquals(testTemplate.getId(), doc.getTemplateId());
        assertEquals(3, doc.getSections().size());
    }

    @Test
    void createFromTemplate_invalidTemplate_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> documentService.createFromTemplate("nonexistent", "Title", null));
    }

    @Test
    void createFromTemplate_nullTitle_usesDefault() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), null, null);
        assertTrue(doc.getTitle().contains("Test Template"));
    }

    @Test
    void createFromTemplate_withMetadata_setsMetadata() {
        Map<String, String> metadata = Map.of("author", "John", "dept", "Engineering");
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Doc", metadata);
        assertEquals("John", doc.getMetadata().get("author"));
        assertEquals("Engineering", doc.getMetadata().get("dept"));
    }

    @Test
    void create_validDocument_succeeds() {
        Document doc = new Document();
        doc.setTitle("Direct Doc");
        Document created = documentService.create(doc);
        assertNotNull(created.getId());
        assertEquals("Direct Doc", created.getTitle());
    }

    @Test
    void create_blankTitle_throwsException() {
        Document doc = new Document();
        doc.setTitle("  ");
        assertThrows(IllegalArgumentException.class, () -> documentService.create(doc));
    }

    @Test
    void findById_existingDocument_returnsDocument() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Find Me", null);
        Optional<Document> found = documentService.findById(doc.getId());
        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        assertTrue(documentService.findById("nonexistent").isEmpty());
    }

    @Test
    void findAll_returnsAllDocuments() {
        documentService.createFromTemplate(testTemplate.getId(), "Doc1", null);
        documentService.createFromTemplate(testTemplate.getId(), "Doc2", null);
        assertEquals(2, documentService.findAll().size());
    }

    @Test
    void findByTemplateId_returnsFilteredDocuments() {
        documentService.createFromTemplate(testTemplate.getId(), "Doc1", null);
        documentService.createFromTemplate(testTemplate.getId(), "Doc2", null);

        List<Document> found = documentService.findByTemplateId(testTemplate.getId());
        assertEquals(2, found.size());
    }

    @Test
    void update_existingDocument_updatesFields() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Original", null);
        Document update = new Document();
        update.setTitle("Updated Title");

        Optional<Document> result = documentService.update(doc.getId(), update);
        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals(2, result.get().getVersion());
    }

    @Test
    void update_nonExistent_returnsEmpty() {
        Document update = new Document();
        update.setTitle("Updated");
        assertTrue(documentService.update("nonexistent", update).isEmpty());
    }

    @Test
    void updateSection_validSection_updatesContent() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Doc", null);
        String sectionId = doc.getSections().get(0).getId();

        Optional<Document> result = documentService.updateSection(doc.getId(), sectionId, "New content");
        assertTrue(result.isPresent());
        assertEquals("New content", result.get().getSections().get(0).getContent());
    }

    @Test
    void updateSection_invalidSection_throwsException() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Doc", null);
        assertThrows(IllegalArgumentException.class,
                () -> documentService.updateSection(doc.getId(), "invalid-section", "content"));
    }

    @Test
    void delete_existingDocument_returnsTrue() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Doc", null);
        assertTrue(documentService.delete(doc.getId()));
        assertTrue(documentService.findById(doc.getId()).isEmpty());
    }

    @Test
    void count_returnsCorrectCount() {
        assertEquals(0, documentService.count());
        documentService.createFromTemplate(testTemplate.getId(), "Doc1", null);
        assertEquals(1, documentService.count());
    }

    @Test
    void createFromTemplate_createsInitialVersion() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Versioned", null);
        assertEquals(1, versionService.getVersionHistory(doc.getId()).size());
    }

    @Test
    void rollback_validVersion_restoresDocument() {
        Document doc = documentService.createFromTemplate(testTemplate.getId(), "Rollback Test", null);
        String sectionId = doc.getSections().get(0).getId();
        documentService.updateSection(doc.getId(), sectionId, "Modified content");

        Optional<Document> rolledBack = documentService.rollback(doc.getId(), 1);
        assertTrue(rolledBack.isPresent());
        assertEquals("", rolledBack.get().getSections().get(0).getContent());
    }
}
