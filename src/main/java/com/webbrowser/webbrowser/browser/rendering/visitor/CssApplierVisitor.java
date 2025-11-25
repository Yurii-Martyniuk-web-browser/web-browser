package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.CssStorage;
import com.webbrowser.webbrowser.browser.rendering.StyleContext;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;

public class CssApplierVisitor implements NodeVisitor {

    public static final String STYLE_CONTEXT_KEY = "style_context";

    @Override
    public void head(Node node) {
        if (!(node instanceof Element element)) {
            return;
        }

        StyleContext context = new StyleContext();
        String tagName = element.tagName().toLowerCase();

        String tagStyle = CssStorage.getStyleForTag(tagName);
        parseAndSet(context, tagStyle);

        String classAttr = element.attr("class");
        if (!classAttr.isEmpty()) {
            String[] classes = classAttr.split("\\s+");
            for (String className : classes) {
                String classStyle = CssStorage.getStyleForClass(className);
                if (!classStyle.isEmpty()) {
                    parseAndSet(context, classStyle);
                }
            }
        }

        String inlineStyle = element.attr("style");
        if (!inlineStyle.isEmpty()) {
            parseAndSet(context, inlineStyle);
        }

        element.attributes().userData(STYLE_CONTEXT_KEY, context);
    }

    @Override
    public void tail(Node node) {
    }

    private void parseAndSet(StyleContext context, String cssString) {
        if (cssString == null || cssString.isEmpty()) {
            return;
        }

        String[] rules = cssString.split(";");
        for (String rule : rules) {
            String[] parts = rule.split(":", 2);
            if (parts.length == 2) {
                context.setProperty(parts[0].trim(), parts[1].trim());
            }
        }
    }
}