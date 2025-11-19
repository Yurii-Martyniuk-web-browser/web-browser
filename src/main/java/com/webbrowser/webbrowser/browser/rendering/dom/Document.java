package com.webbrowser.webbrowser.browser.rendering.dom;

import java.util.Collections;
import java.util.List;


public class Document {
    private Element root;
    private String title = "";

    public Document(Element root) {
        this.root = root;
    }

    // Імітація document.children()
    public List<Element> children() {
        return root != null ? root.children() : Collections.emptyList();
    }

    // Імітація document.title()
    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}