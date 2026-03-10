package com.synthdoc.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Citation {

    public enum CitationFormat {
        APA, MLA, CHICAGO
    }

    private String id;
    private String documentId;
    private String sectionId;
    private String text;
    private String source;
    private CitationFormat format;
    private Map<String, String> metadata;
    private Instant createdAt;

    public Citation() {
        this.id = UUID.randomUUID().toString();
        this.metadata = new HashMap<>();
        this.createdAt = Instant.now();
    }

    @JsonCreator
    public Citation(
            @JsonProperty("documentId") String documentId,
            @JsonProperty("sectionId") String sectionId,
            @JsonProperty("text") String text,
            @JsonProperty("source") String source,
            @JsonProperty("format") CitationFormat format,
            @JsonProperty("metadata") Map<String, String> metadata) {
        this.id = UUID.randomUUID().toString();
        this.documentId = documentId;
        this.sectionId = sectionId;
        this.text = text;
        this.source = source;
        this.format = format;
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public CitationFormat getFormat() { return format; }
    public void setFormat(CitationFormat format) { this.format = format; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
