package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.visitor.NodeVisitor;

public class FxRenderer {

    public void traverse(Document document, NodeVisitor visitor) {
        if (document != null && !document.children().isEmpty()) {
            traverseElement(document.children().getFirst(), visitor);
        }
    }

    private void traverseElement(Element element, NodeVisitor visitor) {
        if (element == null) {
            return;
        }

        visitor.head(element);

        for (Element child : element.children()) {
            traverseElement(child, visitor);
        }

        visitor.tail(element);
    }
}