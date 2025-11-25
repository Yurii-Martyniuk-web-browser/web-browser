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

        String tag = element.tagName().toLowerCase();
        CssStorage.getStyleForTag(tag).forEach(context::setProperty);

        String classAttr = element.attr("class");
        if (classAttr != null && !classAttr.isEmpty()) {
            for (String cls : classAttr.split("\\s+")) {
                CssStorage.getStyleForClass(cls).forEach(context::setProperty);
            }
        }

        String id = element.attr("id");
        if (id != null && !id.isEmpty()) {
            CssStorage.getStyleForId(id).forEach(context::setProperty);
        }

        String inline = element.attr("style");
        if (inline != null && !inline.isBlank()) {

            Map<String, Map<String, String>> parsedInline =
                    com.webbrowser.webbrowser.browser.rendering.CssParser.parse(inline);

            if (!parsedInline.isEmpty()) {
                Map<String, String> inlineRules = parsedInline.values().iterator().next();
                inlineRules.forEach(context::setProperty);
            } else {
                String[] rules = inline.split(";");
                for (String r : rules) {
                    String[] kv = r.split(":", 2);
                    if (kv.length == 2) {
                        context.setProperty(
                                kv[0].trim().toLowerCase(),
                                kv[1].trim()
                        );
                    }
                }
            }
        }

        element.attributes().userData(STYLE_CONTEXT_KEY, context);
    }

    @Override
    public void tail(Node node) { }
}
