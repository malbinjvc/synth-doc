package com.synthdoc.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Document {

    private String id;
    private String templateId;
    private String title;
    private List<Section> sections;
    private Map<String, String> metadata;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;

    public Document() {
        this.id = UUID.randomUUID().toString();
        this.sections = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.version = 1;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @JsonCreator
    public Document(
            @JsonProperty("templateId") String templateId,
            @JsonProperty("title") String title,
            @JsonProperty("sections") List<Section> sections,
            @JsonProperty("metadata") Map<String, String> metadata) {
        this.id = UUID.randomUUID().toString();
        this.templateId = templateId;
        this.title = title;
        this.sections = sections != null ? sections : new ArrayList<>();
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.version = 1;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Document deepCopy() {
        Document copy = new Document();
        copy.id = this.id;
        copy.templateId = this.templateId;
        copy.title = this.title;
        copy.version = this.version;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        copy.metadata = new HashMap<>(this.metadata);
        copy.sections = new ArrayList<>();
        for (Section s : this.sections) {
            copy.sections.add(s.copy());
        }
        return copy;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { this.sections = sections; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
