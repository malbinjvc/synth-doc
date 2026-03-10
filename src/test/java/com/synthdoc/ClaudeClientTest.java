package com.synthdoc;

import com.synthdoc.clients.HttpClaudeClient;
import com.synthdoc.clients.MockClaudeClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClaudeClientTest {

    @Test
    void mockClient_isAvailable_returnsTrue() {
        MockClaudeClient client = new MockClaudeClient();
        assertTrue(client.isAvailable());
    }

    @Test
    void mockClient_generate_returnsContent() {
        MockClaudeClient client = new MockClaudeClient();
        String result = client.generate("Write an introduction about AI", 512);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("comprehensive overview"));
    }

    @Test
    void mockClient_generate_conclusionPrompt_returnsConclusion() {
        MockClaudeClient client = new MockClaudeClient();
        String result = client.generate("Write a conclusion section", 512);
        assertTrue(result.contains("conclusion"));
    }

    @Test
    void mockClient_generate_bodyPrompt_returnsBody() {
        MockClaudeClient client = new MockClaudeClient();
        String result = client.generate("Write the main body content", 512);
        assertTrue(result.contains("analysis"));
    }

    @Test
    void mockClient_registerResponse_overridesDefault() {
        MockClaudeClient client = new MockClaudeClient();
        client.registerResponse("quantum", "Quantum computing is revolutionary");
        String result = client.generate("Tell me about quantum physics", 512);
        assertEquals("Quantum computing is revolutionary", result);
    }

    @Test
    void mockClient_callCount_tracksCallsCorrectly() {
        MockClaudeClient client = new MockClaudeClient();
        assertEquals(0, client.getCallCount());
        client.generate("prompt1", 100);
        client.generate("prompt2", 100);
        assertEquals(2, client.getCallCount());
    }

    @Test
    void mockClient_resetCallCount_resetsToZero() {
        MockClaudeClient client = new MockClaudeClient();
        client.generate("prompt", 100);
        client.resetCallCount();
        assertEquals(0, client.getCallCount());
    }

    @Test
    void mockClient_clearResponses_clearsRegistered() {
        MockClaudeClient client = new MockClaudeClient();
        client.registerResponse("test", "custom response");
        client.clearResponses();
        String result = client.generate("test something generic", 512);
        assertNotEquals("custom response", result);
    }

    @Test
    void httpClient_nullApiKey_isNotAvailable() {
        HttpClaudeClient client = new HttpClaudeClient(null);
        assertFalse(client.isAvailable());
    }

    @Test
    void httpClient_emptyApiKey_isNotAvailable() {
        HttpClaudeClient client = new HttpClaudeClient("");
        assertFalse(client.isAvailable());
    }

    @Test
    void httpClient_withApiKey_isAvailable() {
        HttpClaudeClient client = new HttpClaudeClient("sk-test-key");
        assertTrue(client.isAvailable());
    }

    @Test
    void mockClient_referencePrompt_returnsReferences() {
        MockClaudeClient client = new MockClaudeClient();
        String result = client.generate("Generate a reference list", 512);
        assertTrue(result.contains("Smith"));
    }
}
