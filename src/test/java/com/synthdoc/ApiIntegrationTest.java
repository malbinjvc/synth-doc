package com.synthdoc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.synthdoc.clients.MockClaudeClient;
import com.synthdoc.models.*;
import com.synthdoc.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiIntegrationTest {

    private App application;
    private HttpClient httpClient;
    private String baseUrl;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        application = new App(new MockClaudeClient());
        application.start(0); // Random available port
        int port = application.javalinApp().port();
        baseUrl = "http://localhost:" + port;
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        application.stop();
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthEndpoint_returnsHealthy() throws Exception {
        HttpResponse<String> response = get("/health");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("healthy"));
        assertTrue(response.body().contains("SynthDoc"));
    }

    @Test
    void createTemplate_returnsCreated() throws Exception {
        String json = """
                {"name":"Research Report","description":"A report template","type":"REPORT",
                 "sections":[{"type":"INTRODUCTION","title":"Intro","content":"","order":1}]}
                """;
        HttpResponse<String> response = post("/api/templates", json);
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Research Report"));
    }

    @Test
    void listTemplates_returnsEmptyList() throws Exception {
        HttpResponse<String> response = get("/api/templates");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("["));
    }

    @Test
    void getTemplate_notFound_returns404() throws Exception {
        HttpResponse<String> response = get("/api/templates/nonexistent");
        assertEquals(404, response.statusCode());
    }

    @Test
    void createAndGetTemplate_roundTrip() throws Exception {
        String json = """
                {"name":"Article Template","description":"An article","type":"ARTICLE",
                 "sections":[{"type":"INTRODUCTION","title":"Intro","content":"","order":1}]}
                """;
        HttpResponse<String> createResp = post("/api/templates", json);
        assertEquals(201, createResp.statusCode());

        Map<String, Object> created = objectMapper.readValue(createResp.body(), Map.class);
        String templateId = (String) created.get("id");

        HttpResponse<String> getResp = get("/api/templates/" + templateId);
        assertEquals(200, getResp.statusCode());
        assertTrue(getResp.body().contains("Article Template"));
    }

    @Test
    void deleteTemplate_existing_returns204() throws Exception {
        String json = """
                {"name":"ToDelete","type":"REPORT","sections":[]}
                """;
        HttpResponse<String> createResp = post("/api/templates", json);
        Map<String, Object> created = objectMapper.readValue(createResp.body(), Map.class);
        String id = (String) created.get("id");

        HttpResponse<String> deleteResp = delete("/api/templates/" + id);
        assertEquals(204, deleteResp.statusCode());
    }

    @Test
    void createDocument_fromTemplate_returns201() throws Exception {
        // Create template
        String templateJson = """
                {"name":"Report Template","type":"REPORT",
                 "sections":[{"type":"INTRODUCTION","title":"Intro","content":"","order":1},
                             {"type":"BODY","title":"Body","content":"","order":2}]}
                """;
        HttpResponse<String> tResp = post("/api/templates", templateJson);
        Map<String, Object> template = objectMapper.readValue(tResp.body(), Map.class);
        String templateId = (String) template.get("id");

        // Create document
        String docJson = String.format("""
                {"templateId":"%s","title":"My Report"}
                """, templateId);
        HttpResponse<String> dResp = post("/api/documents", docJson);
        assertEquals(201, dResp.statusCode());
        assertTrue(dResp.body().contains("My Report"));
    }

    @Test
    void getDocument_notFound_returns404() throws Exception {
        HttpResponse<String> response = get("/api/documents/nonexistent");
        assertEquals(404, response.statusCode());
    }

    @Test
    void statsEndpoint_returnsStats() throws Exception {
        HttpResponse<String> response = get("/api/stats");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("totalTemplates"));
        assertTrue(response.body().contains("totalDocuments"));
        assertTrue(response.body().contains("SynthDoc"));
    }

    @Test
    void createCitation_returnsCreated() throws Exception {
        String json = """
                {"documentId":"doc1","sectionId":"sec1","text":"AI in Healthcare",
                 "source":"Smith, J. (2024)","format":"APA"}
                """;
        HttpResponse<String> response = post("/api/citations", json);
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("AI in Healthcare"));
    }

    @Test
    void getDocumentCitations_returnsEmptyList() throws Exception {
        HttpResponse<String> response = get("/api/documents/doc1/citations");
        assertEquals(200, response.statusCode());
    }

    @Test
    void generateDocument_fullWorkflow() throws Exception {
        // Create template
        String templateJson = """
                {"name":"Full Workflow","type":"REPORT",
                 "sections":[{"type":"INTRODUCTION","title":"Introduction","content":"","order":1},
                             {"type":"BODY","title":"Main Content","content":"","order":2},
                             {"type":"CONCLUSION","title":"Conclusion","content":"","order":3}]}
                """;
        HttpResponse<String> tResp = post("/api/templates", templateJson);
        Map<String, Object> template = objectMapper.readValue(tResp.body(), Map.class);
        String templateId = (String) template.get("id");

        // Create document
        String docJson = String.format("""
                {"templateId":"%s","title":"Generated Report"}
                """, templateId);
        HttpResponse<String> dResp = post("/api/documents", docJson);
        Map<String, Object> doc = objectMapper.readValue(dResp.body(), Map.class);
        String docId = (String) doc.get("id");

        // Generate all sections
        String genJson = """
                {"tone":"professional","maxTokens":512}
                """;
        HttpResponse<String> genResp = post("/api/documents/" + docId + "/generate", genJson);
        assertEquals(200, genResp.statusCode());
        assertTrue(genResp.body().contains("comprehensive"));
    }

    @Test
    void exportDocument_jsonFormat_returnsJson() throws Exception {
        // Create template and document
        String templateJson = """
                {"name":"Export Template","type":"SUMMARY",
                 "sections":[{"type":"INTRODUCTION","title":"Intro","content":"Hello","order":1}]}
                """;
        HttpResponse<String> tResp = post("/api/templates", templateJson);
        Map<String, Object> template = objectMapper.readValue(tResp.body(), Map.class);
        String templateId = (String) template.get("id");

        String docJson = String.format("""
                {"templateId":"%s","title":"Export Test Doc"}
                """, templateId);
        HttpResponse<String> dResp = post("/api/documents", docJson);
        Map<String, Object> doc = objectMapper.readValue(dResp.body(), Map.class);
        String docId = (String) doc.get("id");

        HttpResponse<String> exportResp = get("/api/documents/" + docId + "/export?format=json");
        assertEquals(200, exportResp.statusCode());
        assertTrue(exportResp.body().contains("Export Test Doc"));
    }

    @Test
    void versionsEndpoint_returnsVersionHistory() throws Exception {
        // Create template and document
        String templateJson = """
                {"name":"Versioned Template","type":"REPORT",
                 "sections":[{"type":"BODY","title":"Content","content":"","order":1}]}
                """;
        HttpResponse<String> tResp = post("/api/templates", templateJson);
        Map<String, Object> template = objectMapper.readValue(tResp.body(), Map.class);
        String templateId = (String) template.get("id");

        String docJson = String.format("""
                {"templateId":"%s","title":"Versioned Doc"}
                """, templateId);
        HttpResponse<String> dResp = post("/api/documents", docJson);
        Map<String, Object> doc = objectMapper.readValue(dResp.body(), Map.class);
        String docId = (String) doc.get("id");

        HttpResponse<String> vResp = get("/api/documents/" + docId + "/versions");
        assertEquals(200, vResp.statusCode());
        assertTrue(vResp.body().contains("versionNumber"));
    }

    @Test
    void updateTemplate_existingTemplate_updatesFields() throws Exception {
        String json = """
                {"name":"Original","type":"REPORT","sections":[]}
                """;
        HttpResponse<String> createResp = post("/api/templates", json);
        Map<String, Object> created = objectMapper.readValue(createResp.body(), Map.class);
        String id = (String) created.get("id");

        String updateJson = """
                {"name":"Updated Name","description":"New description"}
                """;
        HttpResponse<String> updateResp = put("/api/templates/" + id, updateJson);
        assertEquals(200, updateResp.statusCode());
        assertTrue(updateResp.body().contains("Updated Name"));
    }

    @Test
    void listDocuments_returnsDocuments() throws Exception {
        HttpResponse<String> response = get("/api/documents");
        assertEquals(200, response.statusCode());
    }
}
