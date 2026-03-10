package com.synthdoc;

import com.synthdoc.models.Citation;
import com.synthdoc.models.Citation.CitationFormat;
import com.synthdoc.services.CitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CitationServiceTest {

    private CitationService service;

    @BeforeEach
    void setUp() {
        service = new CitationService();
    }

    @Test
    void create_validCitation_succeeds() {
        Citation c = new Citation("doc1", "sec1", "AI in Healthcare",
                "Smith, J. (2024)", CitationFormat.APA, null);
        Citation created = service.create(c);
        assertNotNull(created.getId());
        assertEquals("doc1", created.getDocumentId());
        assertEquals("AI in Healthcare", created.getText());
    }

    @Test
    void create_nullDocumentId_throwsException() {
        Citation c = new Citation(null, "sec1", "Text", "Source", CitationFormat.APA, null);
        assertThrows(IllegalArgumentException.class, () -> service.create(c));
    }

    @Test
    void create_nullText_throwsException() {
        Citation c = new Citation("doc1", "sec1", null, "Source", CitationFormat.APA, null);
        assertThrows(IllegalArgumentException.class, () -> service.create(c));
    }

    @Test
    void create_nullSource_throwsException() {
        Citation c = new Citation("doc1", "sec1", "Text", null, CitationFormat.APA, null);
        assertThrows(IllegalArgumentException.class, () -> service.create(c));
    }

    @Test
    void create_nullFormat_defaultsToAPA() {
        Citation c = new Citation("doc1", "sec1", "Text", "Source", null, null);
        Citation created = service.create(c);
        assertEquals(CitationFormat.APA, created.getFormat());
    }

    @Test
    void findById_existingCitation_returnsCitation() {
        Citation c = new Citation("doc1", "sec1", "Text", "Source", CitationFormat.APA, null);
        service.create(c);
        Optional<Citation> found = service.findById(c.getId());
        assertTrue(found.isPresent());
        assertEquals("Text", found.get().getText());
    }

    @Test
    void findByDocumentId_returnsMatchingCitations() {
        service.create(new Citation("doc1", "sec1", "Text1", "Source1", CitationFormat.APA, null));
        service.create(new Citation("doc1", "sec2", "Text2", "Source2", CitationFormat.MLA, null));
        service.create(new Citation("doc2", "sec1", "Text3", "Source3", CitationFormat.APA, null));

        List<Citation> doc1Citations = service.findByDocumentId("doc1");
        assertEquals(2, doc1Citations.size());
    }

    @Test
    void findBySectionId_returnsMatchingCitations() {
        service.create(new Citation("doc1", "sec1", "Text1", "Source1", CitationFormat.APA, null));
        service.create(new Citation("doc1", "sec1", "Text2", "Source2", CitationFormat.APA, null));
        service.create(new Citation("doc1", "sec2", "Text3", "Source3", CitationFormat.APA, null));

        List<Citation> sec1Citations = service.findBySectionId("doc1", "sec1");
        assertEquals(2, sec1Citations.size());
    }

    @Test
    void findByFormat_returnsFilteredCitations() {
        service.create(new Citation("doc1", "sec1", "Text1", "Source1", CitationFormat.APA, null));
        service.create(new Citation("doc1", "sec2", "Text2", "Source2", CitationFormat.MLA, null));
        service.create(new Citation("doc1", "sec3", "Text3", "Source3", CitationFormat.APA, null));

        List<Citation> apaCitations = service.findByFormat("doc1", CitationFormat.APA);
        assertEquals(2, apaCitations.size());
    }

    @Test
    void update_existingCitation_updatesFields() {
        Citation c = new Citation("doc1", "sec1", "Original", "OrigSource", CitationFormat.APA, null);
        service.create(c);

        Citation update = new Citation("doc1", "sec1", "Updated Text", "New Source",
                CitationFormat.MLA, null);
        Optional<Citation> result = service.update(c.getId(), update);

        assertTrue(result.isPresent());
        assertEquals("Updated Text", result.get().getText());
        assertEquals("New Source", result.get().getSource());
        assertEquals(CitationFormat.MLA, result.get().getFormat());
    }

    @Test
    void delete_existingCitation_returnsTrue() {
        Citation c = new Citation("doc1", "sec1", "Text", "Source", CitationFormat.APA, null);
        service.create(c);
        assertTrue(service.delete(c.getId()));
        assertTrue(service.findById(c.getId()).isEmpty());
    }

    @Test
    void deleteByDocumentId_removesAllDocCitations() {
        service.create(new Citation("doc1", "sec1", "Text1", "Source1", CitationFormat.APA, null));
        service.create(new Citation("doc1", "sec2", "Text2", "Source2", CitationFormat.APA, null));
        service.create(new Citation("doc2", "sec1", "Text3", "Source3", CitationFormat.APA, null));

        int removed = service.deleteByDocumentId("doc1");
        assertEquals(2, removed);
        assertEquals(1, service.count());
    }

    @Test
    void generateBibliography_withCitations_returnsBibliography() {
        service.create(new Citation("doc1", "sec1", "AI in Healthcare",
                "Smith, J.", CitationFormat.APA, null));
        service.create(new Citation("doc1", "sec2", "Machine Learning Basics",
                "Johnson, A.", CitationFormat.APA, null));

        String bib = service.generateBibliography("doc1");
        assertNotNull(bib);
        assertTrue(bib.contains("Bibliography"));
        assertTrue(bib.contains("Smith"));
        assertTrue(bib.contains("Johnson"));
    }

    @Test
    void generateBibliography_noCitations_returnsMessage() {
        String bib = service.generateBibliography("empty-doc");
        assertTrue(bib.contains("No citations found"));
    }

    @Test
    void countByDocument_returnsCorrectCount() {
        service.create(new Citation("doc1", "sec1", "Text1", "Source1", CitationFormat.APA, null));
        service.create(new Citation("doc1", "sec2", "Text2", "Source2", CitationFormat.APA, null));
        service.create(new Citation("doc2", "sec1", "Text3", "Source3", CitationFormat.APA, null));

        assertEquals(2, service.countByDocument("doc1"));
        assertEquals(1, service.countByDocument("doc2"));
    }
}
