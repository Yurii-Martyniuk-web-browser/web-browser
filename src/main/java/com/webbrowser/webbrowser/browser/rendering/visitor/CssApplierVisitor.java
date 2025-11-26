package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.PageContext;
import com.webbrowser.webbrowser.browser.rendering.css.StyleCalculator;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;

import java.util.Map;

public record CssApplierVisitor(PageContext pageContext) implements NodeVisitor {

    public static final String COMPUTED_STYLE_KEY = "computed_style";

    @Override
    public void head(Node node) {
        if (!(node instanceof Element element)) return;

        Map<String, String> computedStyles = StyleCalculator.computeStyle(element, pageContext);

        element.attributes().userData(COMPUTED_STYLE_KEY, computedStyles);
    }

    @Override
    public void tail(Node node) {
    }
}