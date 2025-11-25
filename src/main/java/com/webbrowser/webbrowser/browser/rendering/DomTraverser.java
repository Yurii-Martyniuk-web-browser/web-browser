package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.browser.rendering.visitor.NodeVisitor;

public class DomTraverser {

    public static void traverse(Node node, NodeVisitor visitor) {
        if (node == null) return;

        visitor.head(node);

        if (node instanceof Element element) {
            for (Node child : element.children()) {
                traverse(child, visitor);
            }
        }

        visitor.tail(node);
    }
}