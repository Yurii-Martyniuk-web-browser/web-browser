package com.webbrowser.webbrowser.browser.rendering.dom;

import java.util.ArrayList;
import java.util.List;

public class Element {
    private final String tagName;
    private final Attributes attributes = new Attributes();
    private final List<Element> children = new ArrayList<>();
    private String ownText = "";

    public Element(String tagName) {
        this.tagName = tagName.toLowerCase();
    }

    public String tagName() {
        return tagName;
    }

    public String attr(String key) {
        return attributes.get(key).toString();
    }

    public Attributes attributes() {
        return attributes;
    }

    public List<Element> children() {
        return children;
    }

    public String text() {
        return ownText;
    }

    public String ownText() {
        return ownText;
    }

    public void setOwnText(String text) {
        this.ownText = text;
    }

    public void addChild(Element child) {
        this.children.add(child);
    }
}