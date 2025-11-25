package com.webbrowser.webbrowser.browser.rendering;

import java.util.HashMap;
import java.util.Map;

public class CssStorage {

    private static final Map<String, Map<String, String>> rules = new HashMap<>();

    public static synchronized void addGlobalStyles(String cssText) {
        if (cssText == null || cssText.isBlank()) return;

        Map<String, Map<String, String>> parsed = CssParser.parse(cssText);

        for (var entry : parsed.entrySet()) {
            String selector = entry.getKey().trim();

            if (selector.contains(" ") ||
                    selector.contains(">") ||
                    selector.contains("+") ||
                    selector.contains("~")) {
                continue;
            }

            if (selector.startsWith(".")) {
                selector = selector.toLowerCase();
            } else if (selector.startsWith("#")) {
                continue;
            } else {
                selector = selector.toLowerCase();
            }

            rules.merge(selector, entry.getValue(), (oldMap, newMap) -> {
                oldMap.putAll(newMap);
                return oldMap;
            });
        }

    }

    public static synchronized Map<String, String> getStyleForTag(String tagName) {
        if (tagName == null) return Map.of();
        return rules.getOrDefault(tagName.toLowerCase(), Map.of());
    }

    public static synchronized Map<String, String> getStyleForClass(String className) {
        if (className == null) return Map.of();
        return rules.getOrDefault("." + className.toLowerCase(), Map.of());
    }

    public static synchronized Map<String, String> getStyleForId(String id) {
        if (id == null) return Map.of();
        return rules.getOrDefault("#" + id, Map.of());
    }
}
