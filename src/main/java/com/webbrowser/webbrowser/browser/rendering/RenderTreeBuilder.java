package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.css.StyleCalculator;
import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.browser.rendering.dom.TextNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record RenderTreeBuilder(PageContext pageContext) {

    private static final Set<String> INHERITABLE_PROPERTIES = Set.of(
            "color", "font-family", "font-size", "font-style", "font-weight",
            "text-align", "text-decoration", "visibility", "line-height", "href"
    );

    private static final Set<String> BLOCK_TAGS = Set.of(
            "html", "body", "div", "p", "blockquote", "pre", "center", "hr", "form",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "table", "thead", "tbody", "tfoot", "tr", "td", "th",
            "main", "header", "footer", "nav", "section", "article", "aside",
            "ul", "ol", "li", "dl", "dt", "dd", "figure", "figcaption"
    );

    public RenderNode build(Document doc) {
        Map<String, String> defaultStyles = new HashMap<>();
        defaultStyles.put("color", "#000000");
        defaultStyles.put("font-size", "16px");
        defaultStyles.put("font-family", "Arial, sans-serif");

        RenderNode root = createRenderNode(doc.getRoot(), defaultStyles);
        if (root != null && root.type == RenderNode.Type.INLINE) {
            root.type = RenderNode.Type.BLOCK;
        }
        return root;
    }

    private RenderNode createRenderNode(Node node, Map<String, String> inheritedStyles) {
        if (node instanceof TextNode textNode) {
            String text = textNode.getText().replace("\n", " ").replace("\r", " ");
            if (text.isBlank() && !text.isEmpty()) text = " ";
            if (text.isEmpty()) return null;

            RenderNode rn = new RenderNode();
            rn.type = RenderNode.Type.TEXT;
            rn.text = text;
            rn.style.putAll(inheritedStyles);
            return rn;
        }

        if (node instanceof Element el) {
            String tagName = el.tagName();
            if (isIgnoredTag(tagName)) return null;

            RenderNode rn = new RenderNode();

            Map<String, String> calculatedStyles = StyleCalculator.computeStyle(el, pageContext);

            rn.style.putAll(inheritedStyles);
            rn.style.putAll(calculatedStyles);

            configureRenderType(rn, el, rn.style);

            Map<String, String> stylesForChildren = new HashMap<>();
            for (Map.Entry<String, String> entry : rn.style.entrySet()) {
                if (INHERITABLE_PROPERTIES.contains(entry.getKey())) {
                    stylesForChildren.put(entry.getKey(), entry.getValue());
                }
            }
            if (tagName.equals("a")) {
                String href = el.getAttribute("href");
                if (href != null) {
                    rn.style.put("href", href);
                    stylesForChildren.put("href", href);
                }
            }

            for (Node child : el.children()) {
                RenderNode childRn = createRenderNode(child, stylesForChildren);
                if (childRn != null) {
                    rn.children.add(childRn);
                }
            }
            return rn;
        }
        return null;
    }

    private boolean isIgnoredTag(String tag) {
        return Set.of("head", "meta", "link", "script", "style", "title", "noscript").contains(tag);
    }

    private void configureRenderType(RenderNode rn, Element el, Map<String, String> styles) {
        String tagName = el.tagName();
        String display = styles.get("display");

        switch (tagName) {
            case "img" -> {
                rn.type = RenderNode.Type.IMAGE;
                rn.src = el.getAttribute("src");
                rn.image = pageContext.getImage(rn.src);
                return;
            }
            case "table" -> {
                rn.type = RenderNode.Type.TABLE;
                return;
            }
            case "tr" -> {
                rn.type = RenderNode.Type.ROW;
                return;
            }
            case "td", "th" -> {
                rn.type = RenderNode.Type.CELL;
                return;
            }
        }

        if (BLOCK_TAGS.contains(tagName)) {
            rn.type = RenderNode.Type.BLOCK;
            rn.style.put("display", "block");
            return;
        }

        if ("block".equalsIgnoreCase(display)) {
            rn.type = RenderNode.Type.BLOCK;
            return;
        }

        rn.type = RenderNode.Type.INLINE;
    }
}