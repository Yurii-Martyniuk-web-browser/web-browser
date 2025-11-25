package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.browser.rendering.dom.TextNode;
import com.webbrowser.webbrowser.browser.rendering.visitor.CssApplierVisitor;

import java.util.Map;
import java.util.Set;

public class RenderTreeBuilder {

    private static final Set<String> IGNORED_TAGS = Set.of(
            "head", "script", "style", "meta", "link", "title", "noscript", "iframe", "svg", "path"
    );

    private Map<String, byte[]> images;

    public RenderNode build(com.webbrowser.webbrowser.browser.rendering.dom.Document doc) {
        return convert(doc.getRoot());
    }

    private RenderNode convert(Node node) {
        if (node instanceof TextNode) {
            RenderNode rn = new RenderNode();
            rn.type = RenderNode.Type.TEXT;
            rn.text = ((TextNode) node).getText();
            return rn;
        }

        if (!(node instanceof Element el)) return null;
        String tagName = el.tagName();

        if (IGNORED_TAGS.contains(tagName)) return null;

        RenderNode rn = new RenderNode();
        rn.style = extractStyles(el);

        switch (tagName) {
            case "html", "body", "div", "p", "blockquote", "pre", "center", "hr", "form", "h1", "h2", "h3", "h4", "h5",
                 "h6", "table", "thead", "tbody", "tfoot", "tr", "td", "th", "main", "header", "footer", "nav",
                 "section", "article", "aside", "figure", "figcaption", "ul", "ol", "li", "dl", "dt", "dd" -> rn.type = RenderNode.Type.BLOCK;

            case "img" -> {
                rn.type = RenderNode.Type.IMAGE;
                rn.src = el.getAttribute("src");
                rn.image = images.get(rn.src);
            }

            default -> rn.type = RenderNode.Type.INLINE;
        }

        if (tagName.equals("a")) {
            rn.style.put("href", el.attr("href"));
            if (!rn.style.containsKey("-fx-text-fill") && !rn.style.containsKey("color")) {
                rn.style.put("color", "blue");
                rn.style.put("text-decoration", "underline");
            }
        }

        for (Node child : el.children()) {
            RenderNode childRn = convert(child);
            if (childRn != null) {
                rn.children.add(childRn);
            }
        }

        return rn;
    }

    private Map<String, String> extractStyles(Element el) {
        Object contextObj = el.attributes().userData(CssApplierVisitor.STYLE_CONTEXT_KEY);
        if (contextObj instanceof StyleContext) {
            return ((StyleContext) contextObj).getStyleProperties();
        }
        return new java.util.HashMap<>();
    }

    public void setImages(Map<String, byte[]> images) {
        this.images = images;
    }
}