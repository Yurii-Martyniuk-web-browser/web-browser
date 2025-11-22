package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.dom.Node;

public interface NodeVisitor {
    void head(Node element);
    void tail(Node element);
}