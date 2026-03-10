package com.synthdoc.clients;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockClaudeClient implements ClaudeClient {

    private final Map<String, String> responses = new ConcurrentHashMap<>();
    private int callCount = 0;

    public MockClaudeClient() {
        // Default deterministic responses based on prompt content
    }

    @Override
    public String generate(String prompt, int maxTokens) {
        callCount++;
        String lowerPrompt = prompt.toLowerCase();

        // Check for custom registered responses first
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (lowerPrompt.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        // Deterministic responses based on section type / prompt keywords
        if (lowerPrompt.contains("introduction")) {
            return "This document provides a comprehensive overview of the subject matter. "
                    + "It explores key concepts, analyzes current trends, and presents actionable insights "
                    + "for stakeholders and decision-makers.";
        } else if (lowerPrompt.contains("conclusion")) {
            return "In conclusion, the analysis demonstrates significant findings that support "
                    + "the initial hypothesis. The evidence gathered throughout this document points "
                    + "to clear recommendations for future action.";
        } else if (lowerPrompt.contains("body") || lowerPrompt.contains("main")) {
            return "The analysis reveals several key findings. First, the data indicates a strong "
                    + "correlation between the identified variables. Second, the methodology employed "
                    + "ensures reliability and validity of results. Third, the implications extend "
                    + "beyond the immediate scope of this study.";
        } else if (lowerPrompt.contains("reference") || lowerPrompt.contains("citation")) {
            return "Smith, J. (2024). Research Methods in Modern Analysis. Academic Press. "
                    + "Johnson, A. & Williams, B. (2023). Data-Driven Decision Making. Oxford University Press.";
        } else if (lowerPrompt.contains("summary") || lowerPrompt.contains("abstract")) {
            return "This document summarizes the key findings and recommendations from the analysis. "
                    + "The research demonstrates a clear path forward with actionable steps.";
        } else if (lowerPrompt.contains("proposal")) {
            return "We propose a comprehensive approach to address the identified challenges. "
                    + "This proposal outlines the methodology, timeline, and expected outcomes.";
        } else {
            return "Generated content for the requested topic. This section covers the essential "
                    + "aspects of the subject matter, providing detailed analysis and supporting evidence "
                    + "for the stated objectives.";
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public void registerResponse(String promptKeyword, String response) {
        responses.put(promptKeyword, response);
    }

    public void clearResponses() {
        responses.clear();
    }

    public int getCallCount() {
        return callCount;
    }

    public void resetCallCount() {
        callCount = 0;
    }
}
