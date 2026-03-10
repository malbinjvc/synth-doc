package com.synthdoc.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerationRequest {

    private String documentId;
    private String sectionId;
    private String tone;
    private int maxTokens;
    private String context;

    public GenerationRequest() {
        this.maxTokens = 1024;
        this.tone = "professional";
    }

    @JsonCreator
    public GenerationRequest(
            @JsonProperty("documentId") String documentId,
            @JsonProperty("sectionId") String sectionId,
            @JsonProperty("tone") String tone,
            @JsonProperty("maxTokens") int maxTokens,
            @JsonProperty("context") String context) {
        this.documentId = documentId;
        this.sectionId = sectionId;
        this.tone = tone != null ? tone : "professional";
        this.maxTokens = maxTokens > 0 ? maxTokens : 1024;
        this.context = context;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
