package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.browser.rendering.visitor.NodeVisitor;

public class DomTraverser {
    // Змінили Element на Node
    public static void traverse(Node node, NodeVisitor visitor) {
        if (node == null) return;

        visitor.head(node);

        // Тільки Element може мати дітей. TextNode дітей не має.
        if (node instanceof Element) {
            Element element = (Element) node;
            for (Node child : element.children()) {
                traverse(child, visitor);
            }
        }

        visitor.tail(node);
    }
}