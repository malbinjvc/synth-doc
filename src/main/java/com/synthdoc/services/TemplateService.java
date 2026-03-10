package com.synthdoc.services;

import com.synthdoc.models.Template;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TemplateService {

    private final Map<String, Template> templates = new ConcurrentHashMap<>();

    public Template create(Template template) {
        if (template.getName() == null || template.getName().isBlank()) {
            throw new IllegalArgumentException("Template name is required");
        }
        if (template.getType() == null) {
            throw new IllegalArgumentException("Template type is required");
        }
        templates.put(template.getId(), template);
        return template;
    }

    public Optional<Template> findById(String id) {
        return Optional.ofNullable(templates.get(id));
    }

    public List<Template> findAll() {
        return new ArrayList<>(templates.values());
    }

    public List<Template> findByType(Template.TemplateType type) {
        return templates.values().stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }

    public Optional<Template> update(String id, Template updated) {
        Template existing = templates.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (updated.getName() != null && !updated.getName().isBlank()) {
            existing.setName(updated.getName());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getType() != null) {
            existing.setType(updated.getType());
        }
        if (updated.getSections() != null && !updated.getSections().isEmpty()) {
            existing.setSections(updated.getSections());
        }
        if (updated.getVariables() != null) {
            existing.setVariables(updated.getVariables());
        }
        return Optional.of(existing);
    }

    public boolean delete(String id) {
        return templates.remove(id) != null;
    }

    public int count() {
        return templates.size();
    }

    public List<Template> search(String query) {
        String lower = query.toLowerCase();
        return templates.values().stream()
                .filter(t -> t.getName().toLowerCase().contains(lower)
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }
}
