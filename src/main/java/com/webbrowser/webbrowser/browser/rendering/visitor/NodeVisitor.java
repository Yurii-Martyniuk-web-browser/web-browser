package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;

public interface NodeVisitor {
    void head(Element element);
    void tail(Element element);
}