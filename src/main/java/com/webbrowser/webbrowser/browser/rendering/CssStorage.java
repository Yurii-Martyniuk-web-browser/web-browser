package com.webbrowser.webbrowser.browser.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssStorage {
    private static final Map<String, String> tagStyles = new HashMap<>();
    private static final Map<String, String> classStyles = new HashMap<>();

    static {
        tagStyles.put("html", "font-family: Arial, sans-serif; line-height: 1.15; font-size: 16px;");
        tagStyles.put("body", "margin: 8px; padding: 0; display: block; background-color: white;");

        tagStyles.put("h1", "font-size: 2em; font-weight: bold; margin: 0.67em 0; display: block;");
        tagStyles.put("h2", "font-size: 1.5em; font-weight: bold; margin: 0.83em 0; display: block;");
        tagStyles.put("h3", "font-size: 1.17em; font-weight: bold; margin: 1em 0; display: block;");
        tagStyles.put("h4", "font-size: 1em; font-weight: bold; margin: 1.33em 0; display: block;");
        tagStyles.put("h5", "font-size: .83em; font-weight: bold; margin: 1.67em 0; display: block;");

        tagStyles.put("p", "margin: 1em 0; font-size: 14px; display: block;");
        tagStyles.put("span", "display: inline;");
        tagStyles.put("strong", "font-weight: bold;");
        tagStyles.put("em", "font-style: italic;");
        tagStyles.put("hr", "border-top: 1px solid #ccc; margin: 0.5em auto; display: block; height: 0;");

        tagStyles.put("a", "color: #0000EE; text-decoration: underline; cursor: pointer;");

        tagStyles.put("ul", "display: block; list-style-type: disc; margin: 1em 0; padding-left: 40px;");
        tagStyles.put("ol", "display: block; list-style-type: decimal; margin: 1em 0; padding-left: 40px;");
        tagStyles.put("li", "display: list-item;");

        tagStyles.put("table", "display: table; border-collapse: collapse; border-spacing: 2px;");
        tagStyles.put("th", "font-weight: bold; text-align: center; padding: 6px; border: 1px solid #aaa;");
        tagStyles.put("td", "padding: 6px; border: 1px solid #aaa;");
        tagStyles.put("tr", "display: table-row;");

        tagStyles.put("input", "border: 1px solid #aaa; padding: 3px; background-color: white;");
        tagStyles.put("button", "padding: 5px 10px; border: 1px solid #ccc; border-radius: 3px; cursor: pointer; background-color: #f0f0f0;");
        tagStyles.put("label", "display: inline-block;");
        classStyles.put("highlight", "background-color: yellow; color: black; padding: 2px 4px; border-radius: 3px;");
        classStyles.put("primary", "background-color: #007bff; color: white;");
        classStyles.put("secondary", "background-color: #6c757d; color: white;");
        classStyles.put("text-center", "text-align: center;");
        classStyles.put("w-100", "width: 100%;");
        classStyles.put("m-0", "margin: 0;");
        classStyles.put("p-3", "padding: 15px;");
        classStyles.put("container", "width: 90%; margin: 0 auto; display: block;");
        classStyles.put("row", "display: block;");
        classStyles.put("col", "display: inline-block; padding: 5px;");
        classStyles.put("invisible", "display: none;");
        classStyles.put("flex", "display: flex;");

        classStyles.put("mw-body", "margin: 10px; padding: 10px; background-color: white; border: 1px solid #ccc;");
        classStyles.put("mw-body-content", "font-family: sans-serif; font-size: 14px; line-height: 1.6;");
        classStyles.put("infobox", "background-color: #f8f9fa; border: 1px solid #a2a9b1; padding: 5px; float: right;");
        classStyles.put("wikitable", "border: 1px solid black; border-collapse: collapse; margin: 10px 0;");
        classStyles.put("toc", "background-color: #f8f9fa; border: 1px solid #a2a9b1; padding: 10px; display: block;");

        classStyles.put("mw-jump-link", "display: none;");
        classStyles.put("noprint", "display: none;");
    }

    public static String getStyleForTag(String tagName) {
        return tagStyles.getOrDefault(tagName.toLowerCase(), "");
    }

    public static String getStyleForClass(String className) {
        return classStyles.getOrDefault(className.toLowerCase(), "");
    }

    public static void addGlobalStyles(String cssText) {
        Pattern pattern = Pattern.compile("([^{]+)\\{([^}]+)}");
        Matcher matcher = pattern.matcher(cssText);

        while (matcher.find()) {
            String selectors = matcher.group(1).trim();
            String properties = matcher.group(2).trim();

            if (properties.isEmpty()) continue;

            Map<String, String> parsedStyles = new HashMap<>();
            String[] rules = properties.split(";");
            for (String rule : rules) {
                if (rule.isEmpty()) continue;
                String[] parts = rule.split(":", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                if (key.equals("border")) {
                    parseBorder(parsedStyles, value, "");
                } else if (key.equals("border-top") || key.equals("border-bottom")
                        || key.equals("border-left") || key.equals("border-right")) {
                    parseBorder(parsedStyles, value, key.substring(7)); // top, bottom, left, right
                } else if (key.equals("padding") || key.equals("margin")) {
                    String[] vals = value.split("\\s+");
                    switch (vals.length) {
                        case 1 -> parsedStyles.put(key, convertToPx(vals[0]));
                        case 2 -> parsedStyles.put(key + "-vertical", convertToPx(vals[0]));
                        case 3 -> parsedStyles.put(key + "-top", convertToPx(vals[0]));
                        case 4 -> {
                            parsedStyles.put(key + "-top", convertToPx(vals[0]));
                            parsedStyles.put(key + "-right", convertToPx(vals[1]));
                            parsedStyles.put(key + "-bottom", convertToPx(vals[2]));
                            parsedStyles.put(key + "-left", convertToPx(vals[3]));
                        }
                    }
                } else {
                    parsedStyles.put(key, value);
                }
            }

            for (String selector : selectors.split(",")) {
                selector = selector.trim();
                if (selector.startsWith(".")) {
                    classStyles.put(selector.substring(1).toLowerCase(), mapToCssString(parsedStyles));
                } else if (!selector.contains("#") && !selector.contains(" ")) {
                    tagStyles.put(selector.toLowerCase(), mapToCssString(parsedStyles));
                }
            }
        }
    }

    private static void parseBorder(Map<String, String> styles, String value, String side) {
        String[] parts = value.split("\\s+");
        String width = null, style = "solid", color = "black";

        for (String part : parts) {
            if (part.matches("\\d+px")) width = part;
            else if (part.matches("solid|dashed|dotted|double|groove|ridge|inset|outset")) style = part;
            else color = part;
        }

        String suffix = side.isEmpty() ? "" : "-" + side;

        if (width != null) styles.put("border" + suffix + "-width", width);
        styles.put("border" + suffix + "-style", style);
        styles.put("border" + suffix + "-color", color);
    }

    private static String mapToCssString(Map<String, String> styles) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : styles.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
        }
        return sb.toString().trim();
    }

    private static String convertToPx(String val) {
        val = val.trim().toLowerCase();
        if (val.endsWith("px")) return val;
        if (val.endsWith("em")) {
            try { return (Double.parseDouble(val.replace("em", "")) * 16) + "px"; }
            catch (NumberFormatException e) { return "0px"; }
        }
        try { Double.parseDouble(val); return val + "px"; }
        catch (NumberFormatException e) { return val; }
    }

}