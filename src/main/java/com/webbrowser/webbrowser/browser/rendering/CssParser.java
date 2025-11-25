package com.webbrowser.webbrowser.browser.rendering;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssParser {

    private static final Pattern BLOCK_PATTERN = Pattern.compile("([^{]+)\\{([^}]*)}", Pattern.DOTALL);

    public static Map<String, Map<String, String>> parse(String cssText) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        if (cssText == null || cssText.isBlank()) return result;

        cssText = cssText.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        Matcher m = BLOCK_PATTERN.matcher(cssText);
        StringBuilder remaining = new StringBuilder(cssText);
        while (m.find()) {
            String selectors = m.group(1).trim();
            String body = m.group(2).trim();
            applySelectors(result, selectors, body);
            int start = m.start();
            int end = m.end();
            for (int i = start; i < end; i++) remaining.setCharAt(i, ' ');
        }

        String rest = remaining.toString();
        String[] lines = rest.split("[\\r\\n]+");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int splitIdx = line.indexOf(": ");
            if (splitIdx < 0) {
                splitIdx = line.indexOf(':');
            }
            if (splitIdx > 0) {
                String sel = line.substring(0, splitIdx).trim();
                String props = line.substring(splitIdx + 1).trim();
                if (!props.endsWith(";")) props = props + ";";
                applySelectors(result, sel, props);
            }
        }

        return result;
    }

    private static void applySelectors(Map<String, Map<String, String>> out, String rawSelectors, String body) {
        if (rawSelectors == null || rawSelectors.isBlank()) return;
        String[] selArr = rawSelectors.split(",");
        for (String rawSel : selArr) {
            String sel = rawSel.trim();
            if (sel.isEmpty()) continue;

            String normalized = sel;
            int colonIdx = normalized.indexOf(':');
            if (colonIdx >= 0) normalized = normalized.substring(0, colonIdx).trim();

            if (normalized.contains(" ") || normalized.contains(">") || normalized.contains("+") || normalized.contains("~")) {
                continue;
            }

            normalized = normalized.replaceAll("\\s+", "");
            if (!normalized.startsWith(".") && !normalized.startsWith("#")) {
                normalized = normalized.toLowerCase();
            }

            Map<String, String> parsedProps = parseProperties(body);
            if (parsedProps.isEmpty()) continue;

            out.merge(normalized, parsedProps, (oldMap, newMap) -> {
                oldMap.putAll(newMap);
                return oldMap;
            });
        }
    }

    private static Map<String, String> parseProperties(String body) {
        Map<String, String> props = new LinkedHashMap<>();
        if (body == null || body.isBlank()) return props;

        String[] declarations = body.split(";");
        for (String decl : declarations) {
            String d = decl.trim();
            if (d.isEmpty()) continue;
            int colon = d.indexOf(':');
            if (colon <= 0) continue;
            String key = d.substring(0, colon).trim().toLowerCase();
            String val = d.substring(colon + 1).trim();
            val = val.replaceAll("!important", "").trim();
            if (key.isEmpty() || val.isEmpty()) continue;

            if (key.equals("margin") || key.equals("padding")) {
                Map<String, String> expanded = expandBox(key, val);
                props.putAll(expanded);
            } else if (key.equals("border")) {
                Map<String, String> expanded = expandBorder(val);
                props.putAll(expanded);
            } else if (key.startsWith("border-") && (key.endsWith("width") || key.endsWith("style") || key.endsWith("color"))) {
                props.put(key, val);
            } else {
                props.put(key, val);
            }
        }

        return props;
    }

    private static Map<String, String> expandBox(String key, String value) {
        Map<String, String> out = new LinkedHashMap<>();
        String[] parts = value.split("\\s+");
        switch (parts.length) {
            case 1 -> {
                out.put(key + "-top", parts[0]);
                out.put(key + "-right", parts[0]);
                out.put(key + "-bottom", parts[0]);
                out.put(key + "-left", parts[0]);
            }
            case 2 -> {
                out.put(key + "-top", parts[0]);
                out.put(key + "-right", parts[1]);
                out.put(key + "-bottom", parts[0]);
                out.put(key + "-left", parts[1]);
            }
            case 3 -> {
                out.put(key + "-top", parts[0]);
                out.put(key + "-right", parts[1]);
                out.put(key + "-bottom", parts[2]);
                out.put(key + "-left", parts[1]);
            }
            default -> {
                out.put(key + "-top", parts[0]);
                out.put(key + "-right", parts[1]);
                out.put(key + "-bottom", parts[2]);
                out.put(key + "-left", parts[3]);
            }
        }
        return out;
    }

    private static Map<String, String> expandBorder(String value) {
        Map<String, String> out = new LinkedHashMap<>();
        String[] parts = value.split("\\s+");
        String width = null, style = null, color = null;
        for (String p : parts) {
            if (p.matches("\\d+px") || p.matches("\\d+")) width = p;
            else if (p.matches("solid|dashed|dotted|double|groove|ridge|inset|outset")) style = p;
            else color = p;
        }
        if (width != null) out.put("border" + "-width", width);
        if (style != null) out.put("border" + "-style", style);
        if (color != null) out.put("border" + "-color", color);
        return out;
    }
}
