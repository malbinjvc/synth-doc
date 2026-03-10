package com.synthdoc;

import com.synthdoc.models.Section;
import com.synthdoc.models.Template;
import com.synthdoc.services.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TemplateServiceTest {

    private TemplateService service;

    @BeforeEach
    void setUp() {
        service = new TemplateService();
    }

    @Test
    void createTemplate_validInput_succeeds() {
        Template t = new Template("Research Report", "A research report template",
                Template.TemplateType.REPORT, List.of(), Map.of("author", "Test"));
        Template created = service.create(t);
        assertNotNull(created.getId());
        assertEquals("Research Report", created.getName());
        assertEquals(Template.TemplateType.REPORT, created.getType());
    }

    @Test
    void createTemplate_nullName_throwsException() {
        Template t = new Template(null, "desc", Template.TemplateType.REPORT, List.of(), null);
        assertThrows(IllegalArgumentException.class, () -> service.create(t));
    }

    @Test
    void createTemplate_blankName_throwsException() {
        Template t = new Template("   ", "desc", Template.TemplateType.REPORT, List.of(), null);
        assertThrows(IllegalArgumentException.class, () -> service.create(t));
    }

    @Test
    void createTemplate_nullType_throwsException() {
        Template t = new Template("Name", "desc", null, List.of(), null);
        assertThrows(IllegalArgumentException.class, () -> service.create(t));
    }

    @Test
    void findById_existingTemplate_returnsTemplate() {
        Template t = new Template("Test", "desc", Template.TemplateType.ARTICLE, List.of(), null);
        service.create(t);
        Optional<Template> found = service.findById(t.getId());
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getName());
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        Optional<Template> found = service.findById("nonexistent");
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsAllTemplates() {
        service.create(new Template("T1", "d1", Template.TemplateType.REPORT, List.of(), null));
        service.create(new Template("T2", "d2", Template.TemplateType.ARTICLE, List.of(), null));
        service.create(new Template("T3", "d3", Template.TemplateType.PROPOSAL, List.of(), null));
        assertEquals(3, service.findAll().size());
    }

    @Test
    void findByType_returnsFilteredTemplates() {
        service.create(new Template("T1", "d1", Template.TemplateType.REPORT, List.of(), null));
        service.create(new Template("T2", "d2", Template.TemplateType.REPORT, List.of(), null));
        service.create(new Template("T3", "d3", Template.TemplateType.ARTICLE, List.of(), null));
        List<Template> reports = service.findByType(Template.TemplateType.REPORT);
        assertEquals(2, reports.size());
    }

    @Test
    void updateTemplate_existingTemplate_updatesFields() {
        Template t = new Template("Original", "desc", Template.TemplateType.REPORT, List.of(), null);
        service.create(t);

        Template update = new Template("Updated", "new desc", Template.TemplateType.ARTICLE, List.of(), null);
        Optional<Template> result = service.update(t.getId(), update);

        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getName());
        assertEquals("new desc", result.get().getDescription());
        assertEquals(Template.TemplateType.ARTICLE, result.get().getType());
    }

    @Test
    void updateTemplate_nonExistent_returnsEmpty() {
        Template update = new Template("Updated", "desc", Template.TemplateType.REPORT, List.of(), null);
        Optional<Template> result = service.update("nonexistent", update);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteTemplate_existingTemplate_returnsTrue() {
        Template t = new Template("ToDelete", "desc", Template.TemplateType.REPORT, List.of(), null);
        service.create(t);
        assertTrue(service.delete(t.getId()));
        assertTrue(service.findById(t.getId()).isEmpty());
    }

    @Test
    void deleteTemplate_nonExistent_returnsFalse() {
        assertFalse(service.delete("nonexistent"));
    }

    @Test
    void count_returnsCorrectCount() {
        assertEquals(0, service.count());
        service.create(new Template("T1", "d1", Template.TemplateType.REPORT, List.of(), null));
        assertEquals(1, service.count());
        service.create(new Template("T2", "d2", Template.TemplateType.ARTICLE, List.of(), null));
        assertEquals(2, service.count());
    }

    @Test
    void search_findsMatchingTemplates() {
        service.create(new Template("AI Research Report", "About artificial intelligence",
                Template.TemplateType.REPORT, List.of(), null));
        service.create(new Template("Finance Summary", "About finance",
                Template.TemplateType.SUMMARY, List.of(), null));

        List<Template> results = service.search("AI");
        assertEquals(1, results.size());
        assertEquals("AI Research Report", results.get(0).getName());
    }

    @Test
    void search_byDescription_findsTemplates() {
        service.create(new Template("Report", "About machine learning topics",
                Template.TemplateType.REPORT, List.of(), null));

        List<Template> results = service.search("machine learning");
        assertEquals(1, results.size());
    }

    @Test
    void createTemplate_withSections_preservesSections() {
        Section s1 = new Section(Section.SectionType.INTRODUCTION, "Intro", "", 1);
        Section s2 = new Section(Section.SectionType.BODY, "Main", "", 2);
        Template t = new Template("WithSections", "desc", Template.TemplateType.REPORT,
                List.of(s1, s2), null);
        Template created = service.create(t);
        assertEquals(2, created.getSections().size());
    }
}
