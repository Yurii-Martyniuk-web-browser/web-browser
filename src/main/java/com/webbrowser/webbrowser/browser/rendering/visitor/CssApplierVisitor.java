package com.webbrowser.webbrowser.browser.rendering.visitor;

import org.jsoup.nodes.Element;

public class CssApplierVisitor implements NodeVisitor {
    @Override
    public void visit(Element element) {
        String style = element.attr("style");
        String cssClass = element.attr("class");

        if (!style.isEmpty()) {
            System.out.println("DEBUG: Applying inline style: " + style + " to <" + element.tagName() + ">");
        }

        if (!cssClass.isEmpty()) {
            System.out.println("DEBUG: Applying CSS class: " + cssClass + " from stylesheet.");
        }
    }
}