package com.webbrowser.webbrowser.browser.rendering.html;

import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.TextNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {

    private static final Set<String> VOID_TAGS = Set.of("img", "br", "hr", "input", "meta", "link");

    public Document parse(String html) {
        Element root = new Element("html");
        Document doc = new Document(root);

        Deque<Element> stack = new ArrayDeque<>();
        stack.push(root);

        int pos = 0;
        while (pos < html.length()) {
            int lt = html.indexOf('<', pos);

            if (lt == -1) {
                String text = html.substring(pos);
                if (!text.isBlank()) {
                    String decodedText = HtmlUtils.decodeEntities(text);
                    if (stack.peek() != null) {
                        stack.peek().addChild(new TextNode(decodedText));
                    }
                }
                break;
            }

            if (lt > pos) {
                String text = html.substring(pos, lt);
                if (!text.isBlank() && stack.peek() != null) {
                    String decodedText = HtmlUtils.decodeEntities(text);
                    if (stack.peek() != null) {
                        stack.peek().addChild(new TextNode(decodedText));
                    }
                }
            }

            int gt = html.indexOf('>', lt);
            if (gt == -1) break;

            String tagContent = html.substring(lt + 1, gt);

            if (tagContent.startsWith("!") || tagContent.startsWith("?")) {
                pos = gt + 1;
                continue;
            }

            handleTag(tagContent, stack, doc);

            pos = gt + 1;
        }

        return doc;
    }

    private void handleTag(String content, Deque<Element> stack, Document doc) {
        content = content.trim();
        if (content.isEmpty()) return;

        boolean isClosing = content.startsWith("/");

        String cleanContent = isClosing ? content.substring(1) : content;

        String[] parts = cleanContent.split("\\s+", 2);
        String tagName = parts[0].toLowerCase();
        String attributes = parts.length > 1 ? parts[1] : "";

        if (isClosing) {
            if (!stack.isEmpty()) {

                if (tagName.equals("title") && stack.peek().tagName().equals("title")) {
                    doc.setTitle(stack.peek() != null ? stack.peek().text() : null);
                }

                if (stack.peek() != null && stack.peek().tagName().equals(tagName)) {
                    stack.pop();
                }
            }
        } else {
            Element el = new Element(tagName);
            parseAttributes(el, attributes);

            if (!stack.isEmpty()) {
                stack.peek().addChild(el);
            }

            if (!VOID_TAGS.contains(tagName) && !content.endsWith("/")) {
                stack.push(el);
            }
        }
    }

    private void parseAttributes(Element el, String rawAttrs) {
        Matcher m = Pattern.compile("(\\w+)=\"([^\"]*)\"").matcher(rawAttrs);
        while (m.find()) {
            el.attributes().put(m.group(1), m.group(2));
        }
    }
}