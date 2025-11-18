package com.webbrowser.webbrowser.browser.rendering.visitor;

import org.jsoup.nodes.Element;


public interface NodeVisitor {

    void head(Element element);
    void tail(Element element);
}