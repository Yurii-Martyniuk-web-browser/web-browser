package com.webbrowser.webbrowser.browser.rendering.dom;

public class TextNode extends Node {
    private String text;

    public TextNode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TextNode{'" + text + "'}";
    }
}