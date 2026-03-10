package com.synthdoc.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class Section {

    public enum SectionType {
        INTRODUCTION, BODY, CONCLUSION, REFERENCES, CUSTOM
    }

    private String id;
    private SectionType type;
    private String title;
    private String content;
    private int order;

    public Section() {
        this.id = UUID.randomUUID().toString();
    }

    @JsonCreator
    public Section(
            @JsonProperty("type") SectionType type,
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("order") int order) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.title = title;
        this.content = content;
        this.order = order;
    }

    public Section copy() {
        Section copy = new Section(this.type, this.title, this.content, this.order);
        copy.id = this.id;
        return copy;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public SectionType getType() { return type; }
    public void setType(SectionType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
