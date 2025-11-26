package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import java.util.HashMap;
import java.util.Map;

public class StyleCalculator {

    private static final Map<String, Map<String, String>> USER_AGENT_STYLES = new HashMap<>();

    static {
        USER_AGENT_STYLES.put("h1", Map.of(
                "display", "block",
                "font-size", "32px",
                "font-weight", "bold",
                "margin-bottom", "10px"
        ));

        USER_AGENT_STYLES.put("h2", Map.of(
                "display", "block",
                "font-size", "24px",
                "font-weight", "bold",
                "margin-bottom", "10px"
        ));

        Map<String, String> boldStyle = Map.of("font-weight", "bold", "display", "inline");
        USER_AGENT_STYLES.put("b", boldStyle);
        USER_AGENT_STYLES.put("strong", boldStyle);

        Map<String, String> italicStyle = Map.of("font-style", "italic", "display", "inline");
        USER_AGENT_STYLES.put("i", italicStyle);
        USER_AGENT_STYLES.put("em", italicStyle);

        USER_AGENT_STYLES.put("a", Map.of(
                "display", "inline",
                "color", "blue",
                "text-decoration", "underline",
                "cursor", "pointer"
        ));
    }

    public static Map<String, String> computeStyle(Element element, PageContext context) {
        Map<String, String> computedStyle = new HashMap<>();

        String tagName = element.tagName().toLowerCase();

        if (USER_AGENT_STYLES.containsKey(tagName)) {
            computedStyle.putAll(USER_AGENT_STYLES.get(tagName));
        } else {
            computedStyle.put("display", "inline");
        }

        if (context != null) {
            for (CssRule rule : context.getCssRules()) {
                if (matches(element, rule.selector)) {
                    computedStyle.putAll(rule.properties);
                }
            }
        }

        String inlineStyle = element.getAttribute("style");
        if (inlineStyle != null && !inlineStyle.isEmpty()) {
            computedStyle.putAll(CssParser.parseProperties(inlineStyle));
        }

        return computedStyle;
    }

    private static boolean matches(Element el, String selector) {
        if (selector.startsWith("#")) {
            String id = el.getAttribute("id");
            return id != null && id.equals(selector.substring(1));
        }
        if (selector.startsWith(".")) {
            String classes = el.getAttribute("class");
            return classes != null && classes.contains(selector.substring(1));
        }
        return selector.equalsIgnoreCase(el.tagName());
    }

    private static boolean isBlockElement(String tag) {
        return switch (tag) {
            case "html", "body", "div", "p", "h1", "h2", "h3", "ul", "li", "header", "footer", "section" -> true;
            default -> false;
        };
    }
}