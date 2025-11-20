package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.browser.rendering.dom.TextNode;
import com.webbrowser.webbrowser.browser.rendering.visitor.CssApplierVisitor;

import java.util.Map;
import java.util.Set;

public class RenderTreeBuilder {

    // Ігноруємо ці теги при побудові візуального дерева
    private static final Set<String> IGNORED_TAGS = Set.of(
            "head", "script", "style", "meta", "link", "title", "noscript", "iframe", "svg", "path"
    );

    public RenderNode build(com.webbrowser.webbrowser.browser.rendering.dom.Document doc) {
        return convert(doc.getRoot());
    }

    private RenderNode convert(Node node) {
        // Якщо це просто текст
        if (node instanceof TextNode) {
            RenderNode rn = new RenderNode();
            rn.type = RenderNode.Type.TEXT;
            rn.text = ((TextNode) node).getText();
            return rn;
        }

        if (!(node instanceof Element)) return null;
        Element el = (Element) node;
        String tagName = el.tagName();

        if (IGNORED_TAGS.contains(tagName)) return null;

        RenderNode rn = new RenderNode();
        rn.style = extractStyles(el);

        // === ГОЛОВНА ЗМІНА ТУТ ===
        // Визначаємо, які теги є блоками (новий рядок), а які інлайн (в рядок)
        switch (tagName) {
            // Стандартні блоки
            case "html", "body", "div", "p", "blockquote", "pre", "center", "hr", "form" -> rn.type = RenderNode.Type.BLOCK;

            // HTML5 структурні блоки (Вікіпедія складається з них)
            case "main", "header", "footer", "nav", "section", "article", "aside", "figure", "figcaption" -> rn.type = RenderNode.Type.BLOCK;

            // Заголовки
            case "h1", "h2", "h3", "h4", "h5", "h6" -> rn.type = RenderNode.Type.BLOCK;

            // Списки
            case "ul", "ol", "li", "dl", "dt", "dd" -> rn.type = RenderNode.Type.BLOCK;

            // Таблиці (спрощено трактуємо як блоки)
            case "table", "thead", "tbody", "tfoot", "tr", "td", "th" -> rn.type = RenderNode.Type.BLOCK;

            // Картинки
            case "img" -> {
                rn.type = RenderNode.Type.IMAGE;
                rn.src = el.attr("src");
            }

            // Всі інші (a, span, b, strong, i...) залишаються INLINE
            default -> rn.type = RenderNode.Type.INLINE;
        }

        // Спеціальна обробка для посилань (<a>)
        if (tagName.equals("a")) {
            rn.style.put("href", el.attr("href"));
            // Додаємо синій колір, якщо він не заданий CSS-ом
            if (!rn.style.containsKey("-fx-text-fill") && !rn.style.containsKey("color")) {
                rn.style.put("color", "blue");
                rn.style.put("text-decoration", "underline");
            }
        }

        // Рекурсивно обробляємо дітей
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
}