package com.synthdoc.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Template {

    public enum TemplateType {
        REPORT, ARTICLE, PROPOSAL, SUMMARY, ANALYSIS
    }

    private String id;
    private String name;
    private String description;
    private TemplateType type;
    private List<Section> sections;
    private Map<String, String> variables;
    private Instant createdAt;

    public Template() {
        this.id = UUID.randomUUID().toString();
        this.sections = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    @JsonCreator
    public Template(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("type") TemplateType type,
            @JsonProperty("sections") List<Section> sections,
            @JsonProperty("variables") Map<String, String> variables) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.type = type;
        this.sections = sections != null ? sections : new ArrayList<>();
        this.variables = variables;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TemplateType getType() { return type; }
    public void setType(TemplateType type) { this.type = type; }
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { this.sections = sections; }
    public Map<String, String> getVariables() { return variables; }
    public void setVariables(Map<String, String> variables) { this.variables = variables; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
