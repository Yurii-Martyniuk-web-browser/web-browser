package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.CssStorage;
import com.webbrowser.webbrowser.browser.rendering.StyleContext;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;

import java.util.Map;

public class CssApplierVisitor implements NodeVisitor {

    public static final String STYLE_CONTEXT_KEY = "style_context";

    @Override
    public void head(Node node) {
        if (!(node instanceof Element element)) return;

        StyleContext context = new StyleContext();
        String tagName = element.tagName().toLowerCase();

        // 1. Стиль тегу
        String tagStyle = CssStorage.getStyleForTag(tagName);
        parseAndSet(context, tagStyle);

        // 2. Класи
        String classAttr = element.attr("class");
        if (!classAttr.isEmpty()) {
            String[] classes = classAttr.split("\\s+");
            for (String className : classes) {
                String classStyle = CssStorage.getStyleForClass(className);
                parseAndSet(context, classStyle);
            }
        }

        // 3. Inline стиль
        String inlineStyle = element.attr("style");
        parseAndSet(context, inlineStyle);

        element.attributes().userData(STYLE_CONTEXT_KEY, context);
    }

    @Override
    public void tail(Node node) { }

    private void parseAndSet(StyleContext context, String cssString) {
        if (cssString == null || cssString.isEmpty()) return;

        String[] rules = cssString.split(";");
        for (String rule : rules) {
            String[] parts = rule.split(":", 2);
            if (parts.length != 2) continue;

            String key = parts[0].trim();
            String value = parts[1].trim();

            if (key.startsWith("border")) parseBorderProperty(context.getStyleProperties(), key, value);
            else if (key.equals("margin") || key.equals("padding")) parseBoxProperty(context.getStyleProperties(), key, value);
            else context.setProperty(key, value);
        }
    }

    private void parseBorderProperty(Map<String, String> styles, String key, String value) {
        String[] parts = value.split("\\s+");
        String width = null, style = null, color = null;

        for (String part : parts) {
            if (part.matches("\\d+px")) width = part;
            else if (part.matches("solid|dashed|dotted|double|groove|ridge|inset|outset")) style = part;
            else color = part;
        }

        String suffix = "";
        if (key.contains("-top")) suffix = "-top";
        else if (key.contains("-bottom")) suffix = "-bottom";
        else if (key.contains("-left")) suffix = "-left";
        else if (key.contains("-right")) suffix = "-right";

        if (width != null) styles.put("border" + suffix + "-width", width);
        if (style != null) styles.put("border" + suffix + "-style", style);
        if (color != null) styles.put("border" + suffix + "-color", color);
    }

    private void parseBoxProperty(Map<String, String> styles, String key, String value) {
        String[] vals = value.split("\\s+");
        switch (vals.length) {
            case 1 -> styles.put(key, vals[0]);
            case 2 -> {
                styles.put(key + "-vertical", vals[0]);
                styles.put(key + "-horizontal", vals[1]);
            }
            case 3 -> {
                styles.put(key + "-top", vals[0]);
                styles.put(key + "-horizontal", vals[1]);
                styles.put(key + "-bottom", vals[2]);
            }
            case 4 -> {
                styles.put(key + "-top", vals[0]);
                styles.put(key + "-right", vals[1]);
                styles.put(key + "-bottom", vals[2]);
                styles.put(key + "-left", vals[3]);
            }
        }
    }
}
