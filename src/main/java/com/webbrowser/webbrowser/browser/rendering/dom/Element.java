package com.webbrowser.webbrowser.browser.rendering.dom;

import java.util.ArrayList;
import java.util.List;

public class Element extends Node {
    private final String tagName;
    private final Attributes attributes = new Attributes();
    private final List<Node> children = new ArrayList<>();

    public Element(String tagName) {
        this.tagName = tagName.toLowerCase();
    }

    public String tagName() {
        return tagName;
    }

    public String attr(String key) {
        return attributes.get(key).toString();
    }

    public String getAttribute(String key) {
        return attributes.getAttr(key);
    }

    public Attributes attributes() {
        return attributes;
    }

    public List<Node> children() {
        return children;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public String text() {
        StringBuilder sb = new StringBuilder();
        for (Node child : children) {
            if (child instanceof TextNode) {
                sb.append(((TextNode) child).getText());
            } else if (child instanceof Element) {
                sb.append(((Element) child).text());
            }
        }
        return sb.toString();
    }
}