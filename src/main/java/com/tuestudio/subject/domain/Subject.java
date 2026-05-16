package com.tuestudio.subject.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Subject {

    private final SubjectId id;
    private String canonicalName;
    private final Set<String> aliases;
    private String icon;

    private Subject(SubjectId id, String canonicalName, Set<String> aliases, String icon) {
        this.id = id;
        this.canonicalName = validateName(canonicalName);
        this.aliases = new LinkedHashSet<>(aliases);
        this.icon = normalizeIcon(icon);
    }

    public static Subject create(SubjectId id, String canonicalName) {
        return new Subject(id, canonicalName, new LinkedHashSet<>(), null);
    }

    public static Subject create(SubjectId id, String canonicalName, String icon) {
        return new Subject(id, canonicalName, new LinkedHashSet<>(), icon);
    }

    public static Subject rehydrate(SubjectId id, String canonicalName, Set<String> aliases, String icon) {
        return new Subject(id, canonicalName, aliases, icon);
    }

    public void rename(String newName) {
        String validated = validateName(newName);
        if (aliasesContainsIgnoreCase(validated)) {
            throw new IllegalArgumentException("Cannot rename to an existing alias: " + newName);
        }
        this.canonicalName = validated;
    }

    public void addAlias(String alias) {
        String trimmed = validateAlias(alias);
        if (trimmed.equalsIgnoreCase(canonicalName)) {
            throw new IllegalArgumentException("Alias collides with canonical name: " + alias);
        }
        if (aliasesContainsIgnoreCase(trimmed)) return;
        aliases.add(trimmed);
    }

    public void removeAlias(String alias) {
        aliases.removeIf(a -> a.equalsIgnoreCase(alias));
    }

    public void changeIcon(String newIcon) {
        this.icon = normalizeIcon(newIcon);
    }

    public SubjectId id() { return id; }
    public String canonicalName() { return canonicalName; }
    public Set<String> aliases() { return Collections.unmodifiableSet(aliases); }
    public String icon() { return icon; }

    private boolean aliasesContainsIgnoreCase(String value) {
        return aliases.stream().anyMatch(a -> a.equalsIgnoreCase(value));
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be blank");
        String trimmed = name.trim();
        if (trimmed.length() > 255) throw new IllegalArgumentException("Name too long");
        return trimmed;
    }

    private static String validateAlias(String alias) {
        if (alias == null || alias.isBlank()) throw new IllegalArgumentException("Alias must not be blank");
        String trimmed = alias.trim();
        if (trimmed.length() > 255) throw new IllegalArgumentException("Alias too long");
        return trimmed;
    }

    private static String normalizeIcon(String icon) {
        if (icon == null || icon.isBlank()) return null;
        return icon.trim();
    }
}
