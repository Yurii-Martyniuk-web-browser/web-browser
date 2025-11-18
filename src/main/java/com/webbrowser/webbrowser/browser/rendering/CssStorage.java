package com.webbrowser.webbrowser.browser.rendering;

import java.util.HashMap;
import java.util.Map;

public class CssStorage {
    private static final Map<String, String> tagStyles = new HashMap<>();
    private static final Map<String, String> classStyles = new HashMap<>();

    static {
        tagStyles.put("p", "margin-bottom: 1em; font-size: 14px;");
        tagStyles.put("a", "text-decoration: none; color: blue;");
        tagStyles.put("h1", "font-size: 32px; font-weight: bold; margin-top: 1em;");

        classStyles.put("highlight", "background-color: yellow; color: black;");
        classStyles.put("button", "border: 1px solid gray; padding: 5px;");
    }

    public static String getStyleForTag(String tagName) {
        return tagStyles.getOrDefault(tagName.toLowerCase(), "");
    }

    public static String getStyleForClass(String className) {
        return classStyles.getOrDefault(className.toLowerCase(), "");
    }
}