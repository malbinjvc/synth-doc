package com.synthdoc;

import com.synthdoc.models.Document;
import com.synthdoc.models.DocumentVersion;
import com.synthdoc.models.Section;
import com.synthdoc.services.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VersionServiceTest {

    private VersionService service;

    @BeforeEach
    void setUp() {
        service = new VersionService();
    }

    private Document createTestDoc(String title) {
        Document doc = new Document();
        doc.setTitle(title);
        Section s = new Section(Section.SectionType.BODY, "Test Section", "Content", 1);
        doc.setSections(new ArrayList<>(List.of(s)));
        return doc;
    }

    @Test
    void saveVersion_createsVersionHistory() {
        Document doc = createTestDoc("V1 Doc");
        service.saveVersion("doc1", doc, "Initial");

        List<DocumentVersion> history = service.getVersionHistory("doc1");
        assertEquals(1, history.size());
        assertEquals(1, history.get(0).getVersionNumber());
        assertEquals("Initial", history.get(0).getChangeDescription());
    }

    @Test
    void saveVersion_incrementsVersionNumber() {
        service.saveVersion("doc1", createTestDoc("V1"), "First");
        service.saveVersion("doc1", createTestDoc("V2"), "Second");
        service.saveVersion("doc1", createTestDoc("V3"), "Third");

        List<DocumentVersion> history = service.getVersionHistory("doc1");
        assertEquals(3, history.size());
        assertEquals(1, history.get(0).getVersionNumber());
        assertEquals(2, history.get(1).getVersionNumber());
        assertEquals(3, history.get(2).getVersionNumber());
    }

    @Test
    void getVersion_existingVersion_returnsVersion() {
        service.saveVersion("doc1", createTestDoc("V1"), "First");
        service.saveVersion("doc1", createTestDoc("V2"), "Second");

        DocumentVersion v = service.getVersion("doc1", 1);
        assertNotNull(v);
        assertEquals(1, v.getVersionNumber());
        assertEquals("V1", v.getSnapshot().getTitle());
    }

    @Test
    void getVersion_nonExistentVersion_returnsNull() {
        service.saveVersion("doc1", createTestDoc("V1"), "First");
        assertNull(service.getVersion("doc1", 99));
    }

    @Test
    void getVersion_nonExistentDoc_returnsNull() {
        assertNull(service.getVersion("nonexistent", 1));
    }

    @Test
    void rollback_existingVersion_returnsSnapshot() {
        Document v1 = createTestDoc("Original Title");
        service.saveVersion("doc1", v1.deepCopy(), "First");

        Document v2 = createTestDoc("Modified Title");
        service.saveVersion("doc1", v2.deepCopy(), "Second");

        Document restored = service.rollback("doc1", 1);
        assertNotNull(restored);
        assertEquals("Original Title", restored.getTitle());
    }

    @Test
    void rollback_nonExistentVersion_returnsNull() {
        assertNull(service.rollback("doc1", 5));
    }

    @Test
    void diff_twoVersions_showsDifferences() {
        Document v1 = createTestDoc("Title V1");
        service.saveVersion("doc1", v1.deepCopy(), "First");

        Document v2 = createTestDoc("Title V2");
        service.saveVersion("doc1", v2.deepCopy(), "Second");

        String diffResult = service.diff("doc1", 1, 2);
        assertNotNull(diffResult);
        assertTrue(diffResult.contains("Title V1"));
        assertTrue(diffResult.contains("Title V2"));
    }

    @Test
    void diff_invalidVersion_throwsException() {
        service.saveVersion("doc1", createTestDoc("V1"), "First");
        assertThrows(IllegalArgumentException.class, () -> service.diff("doc1", 1, 99));
    }

    @Test
    void getLatestVersionNumber_returnsCorrectNumber() {
        service.saveVersion("doc1", createTestDoc("V1"), "First");
        service.saveVersion("doc1", createTestDoc("V2"), "Second");
        assertEquals(2, service.getLatestVersionNumber("doc1"));
    }

    @Test
    void getLatestVersionNumber_noVersions_returnsZero() {
        assertEquals(0, service.getLatestVersionNumber("nonexistent"));
    }

    @Test
    void totalVersions_returnsGlobalCount() {
        service.saveVersion("doc1", createTestDoc("V1"), "First");
        service.saveVersion("doc1", createTestDoc("V2"), "Second");
        service.saveVersion("doc2", createTestDoc("V1"), "First");
        assertEquals(3, service.totalVersions());
    }

    @Test
    void clearHistory_removesAllVersions() {
        service.saveVersion("doc1", createTestDoc("V1"), "First");
        service.saveVersion("doc1", createTestDoc("V2"), "Second");
        service.clearHistory("doc1");
        assertTrue(service.getVersionHistory("doc1").isEmpty());
    }

    @Test
    void getVersionHistory_emptyDoc_returnsEmptyList() {
        List<DocumentVersion> history = service.getVersionHistory("nonexistent");
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}
