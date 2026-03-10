package com.synthdoc;

import com.synthdoc.clients.MockClaudeClient;
import com.synthdoc.models.Document;
import com.synthdoc.models.GenerationRequest;
import com.synthdoc.models.Section;
import com.synthdoc.models.Template;
import com.synthdoc.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenerationServiceTest {

    private GenerationService generationService;
    private DocumentService documentService;
    private MockClaudeClient mockClient;
    private Document testDoc;

    @BeforeEach
    void setUp() {
        mockClient = new MockClaudeClient();
        TemplateService templateService = new TemplateService();
        VersionService versionService = new VersionService();
        documentService = new DocumentService(templateService, versionService);
        generationService = new GenerationService(mockClient, documentService, versionService);

        Section s1 = new Section(Section.SectionType.INTRODUCTION, "Introduction", "", 1);
        Section s2 = new Section(Section.SectionType.BODY, "Main Body", "", 2);
        Section s3 = new Section(Section.SectionType.CONCLUSION, "Conclusion", "", 3);
        Template template = new Template("Test", "desc", Template.TemplateType.REPORT,
                List.of(s1, s2, s3), null);
        templateService.create(template);
        testDoc = documentService.createFromTemplate(template.getId(), "AI Report", null);
    }

    @Test
    void generateAllSections_populatesAllSections() {
        GenerationRequest request = new GenerationRequest(testDoc.getId(), null, "professional", 512, null);
        Document result = generationService.generateAllSections(testDoc.getId(), request);

        for (Section s : result.getSections()) {
            assertNotNull(s.getContent());
            assertFalse(s.getContent().isEmpty());
        }
    }

    @Test
    void generateAllSections_incrementsVersion() {
        int initialVersion = testDoc.getVersion();
        GenerationRequest request = new GenerationRequest(testDoc.getId(), null, "professional", 512, null);
        Document result = generationService.generateAllSections(testDoc.getId(), request);
        assertTrue(result.getVersion() > initialVersion);
    }

    @Test
    void generateSection_populatesSingleSection() {
        String sectionId = testDoc.getSections().get(0).getId();
        GenerationRequest request = new GenerationRequest(testDoc.getId(), sectionId, "formal", 256, null);
        Document result = generationService.generateSection(testDoc.getId(), sectionId, request);

        assertNotNull(result.getSections().get(0).getContent());
        assertFalse(result.getSections().get(0).getContent().isEmpty());
    }

    @Test
    void generateSection_invalidDocId_throwsException() {
        GenerationRequest request = new GenerationRequest("invalid", "s1", "professional", 256, null);
        assertThrows(IllegalArgumentException.class,
                () -> generationService.generateSection("invalid", "s1", request));
    }

    @Test
    void generateSection_invalidSectionId_throwsException() {
        GenerationRequest request = new GenerationRequest(testDoc.getId(), "invalid", "professional", 256, null);
        assertThrows(IllegalArgumentException.class,
                () -> generationService.generateSection(testDoc.getId(), "invalid", request));
    }

    @Test
    void generateContent_callsClient() {
        String content = generationService.generateContent("Write an introduction", 512);
        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    void isClientAvailable_mockClient_returnsTrue() {
        assertTrue(generationService.isClientAvailable());
    }

    @Test
    void generateAllSections_callsClientForEachSection() {
        mockClient.resetCallCount();
        GenerationRequest request = new GenerationRequest(testDoc.getId(), null, "professional", 512, null);
        generationService.generateAllSections(testDoc.getId(), request);
        assertEquals(3, mockClient.getCallCount());
    }

    @Test
    void generateSection_withContext_includesContext() {
        mockClient.registerResponse("quantum computing", "Quantum computing content here");
        String sectionId = testDoc.getSections().get(0).getId();
        GenerationRequest request = new GenerationRequest(
                testDoc.getId(), sectionId, "academic", 512, "Focus on quantum computing");
        Document result = generationService.generateSection(testDoc.getId(), sectionId, request);
        assertNotNull(result.getSections().get(0).getContent());
    }

    @Test
    void generateAllSections_invalidDoc_throwsException() {
        GenerationRequest request = new GenerationRequest("nope", null, "professional", 512, null);
        assertThrows(IllegalArgumentException.class,
                () -> generationService.generateAllSections("nope", request));
    }
}
