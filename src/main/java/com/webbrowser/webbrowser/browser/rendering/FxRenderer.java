package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.visitor.NodeVisitor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class FxRenderer {

    public void traverse(Document document, NodeVisitor visitor) {
        if (document != null && !document.children().isEmpty()) {
            traverseElement(document.children().first(), visitor);
        }
    }

    private void traverseElement(Element element, NodeVisitor visitor) {
        if (element == null) {
            return;
        }

        visitor.visit(element);

        for (Element child : element.children()) {
            traverseElement(child, visitor);
        }
    }
}