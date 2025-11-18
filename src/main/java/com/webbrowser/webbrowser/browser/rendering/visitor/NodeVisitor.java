package com.webbrowser.webbrowser.browser.rendering.visitor;

import org.jsoup.nodes.Element;


public interface NodeVisitor {

    void visit(Element element);

    default void preVisit(Element element) {}
    default void postVisit(Element element) {}
}