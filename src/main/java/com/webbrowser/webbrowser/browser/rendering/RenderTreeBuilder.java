package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.browser.rendering.dom.TextNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RenderTreeBuilder {

    private final PageContext pageContext;

    // Властивості, які передаються від батька до дітей
    private static final Set<String> INHERITABLE_PROPERTIES = Set.of(
            "color", "font-family", "font-size", "font-style", "font-weight",
            "text-align", "text-decoration", "visibility", "line-height", "href"
    );

    // --- ВИПРАВЛЕННЯ: Повертаємо список блокових тегів ---
    // Це страховка: якщо StyleCalculator не спрацював, ми все одно знаємо, що це блок.
    private static final Set<String> BLOCK_TAGS = Set.of(
            "html", "body", "div", "p", "blockquote", "pre", "center", "hr", "form",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "table", "thead", "tbody", "tfoot", "tr", "td", "th",
            "main", "header", "footer", "nav", "section", "article", "aside",
            "ul", "ol", "li", "dl", "dt", "dd", "figure", "figcaption"
    );

    public RenderTreeBuilder(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public RenderNode build(com.webbrowser.webbrowser.browser.rendering.dom.Document doc) {
        Map<String, String> defaultStyles = new HashMap<>();
        defaultStyles.put("color", "#000000");
        defaultStyles.put("font-size", "16px");
        defaultStyles.put("font-family", "Arial, sans-serif"); // Додав дефолтний шрифт

        // Якщо це HTML/BODY, примусово робимо їх блоками, щоб сторінка мала структуру
        RenderNode root = createRenderNode(doc.getRoot(), defaultStyles);
        if (root != null && root.type == RenderNode.Type.INLINE) {
            root.type = RenderNode.Type.BLOCK;
        }
        return root;
    }

    private RenderNode createRenderNode(Node node, Map<String, String> inheritedStyles) {
        if (node instanceof TextNode textNode) {
            String text = textNode.getText().replace("\n", " ").replace("\r", " ");
            // Якщо текст складається тільки з пробілів, скорочуємо до одного
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

            // 1. Рахуємо стилі
            Map<String, String> calculatedStyles = StyleCalculator.computeStyle(el, pageContext);

            // 2. Об'єднуємо (успадковані + власні)
            rn.style.putAll(inheritedStyles);
            rn.style.putAll(calculatedStyles);

            // 3. Визначаємо тип (BLOCK / INLINE / IMAGE)
            // Тут була помилка: ми покладалися лише на style.get("display")
            configureRenderType(rn, el, rn.style);

            // 4. Готуємо стилі для дітей
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

    // --- ОНОВЛЕНИЙ МЕТОД ---
    private void configureRenderType(RenderNode rn, Element el, Map<String, String> styles) {
        String tagName = el.tagName();
        String display = styles.get("display");

        // 1. Картинки завжди окремо
        if (tagName.equals("img")) {
            rn.type = RenderNode.Type.IMAGE;
            rn.src = el.getAttribute("src");
            rn.image = pageContext.getImage(rn.src);
            return;
        }

        // 2. Якщо це відомий БЛОКОВИЙ тег (div, p, h1...) — примусово робимо БЛОКОМ,
        // навіть якщо StyleCalculator помилково повернув "inline".
        if (BLOCK_TAGS.contains(tagName)) {
            rn.type = RenderNode.Type.BLOCK;
            rn.style.put("display", "block"); // Виправляємо стиль
            return;
        }

        // 3. Якщо CSS явно вимагає BLOCK (наприклад <span style="display:block">)
        if ("block".equalsIgnoreCase(display)) {
            rn.type = RenderNode.Type.BLOCK;
            return;
        }

        // 4. В усіх інших випадках — INLINE
        rn.type = RenderNode.Type.INLINE;
    }
}