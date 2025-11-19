package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {

    private static final Set<String> VOID_ELEMENTS = Set.of("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr");

    private static final Pattern TAG_PATTERN = Pattern.compile("<(\\/?)([a-zA-Z0-9]+)([^>]*)>");
    private static final Pattern ATTR_PATTERN = Pattern.compile("([a-zA-Z0-9\\-]+)=\"([^\"]*)\"");

    public Document parse(String html) {
        int bodyStart = html.indexOf("<body>");
        int bodyEnd = html.lastIndexOf("</body>");

        Element body = new Element("body");
        Document doc = new Document(body);
        int titleStart = html.indexOf("<title>");
        int titleEnd = html.indexOf("</title>");
        if (titleStart != -1 && titleEnd != -1) {
            doc.setTitle(html.substring(titleStart + 7, titleEnd));
        }

        if (bodyStart != -1 && bodyEnd != -1) {
            String bodyContent = html.substring(bodyStart + 6, bodyEnd);
            parseContent(body, bodyContent);
        } else {
        }
        return doc;
    }

    private void parseContent(Element parent, String content) {
        Matcher matcher = TAG_PATTERN.matcher(content);
        int lastEnd = 0;

        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            String closingSlash = matcher.group(1);
            String tagName = matcher.group(2).toLowerCase();
            String attrsString = matcher.group(3).trim();
            int tagStart = matcher.start();
            int tagEnd = matcher.end();

            String text = content.substring(lastEnd, tagStart);
            if (!text.trim().isEmpty()) {
                parent.setOwnText(parent.ownText() + text);
            }

            if (closingSlash.isEmpty()) {
                Element newElement = new Element(tagName);

                parseAttributes(newElement, attrsString);

                if (VOID_ELEMENTS.contains(tagName)) {
                    parent.addChild(newElement);
                    lastEnd = tagEnd;
                    continue;
                }

                String closingTag = "</" + tagName + ">";
                int contentEnd = findClosingTag(content, tagEnd, closingTag);

                if (contentEnd != -1) {
                    String subContent = content.substring(tagEnd, contentEnd);

                    parseContent(newElement, subContent);
                    parent.addChild(newElement);

                    lastEnd = contentEnd + closingTag.length();
                    matcher.region(lastEnd, content.length());
                    continue;
                }
            }
            lastEnd = tagEnd;
        }

        String remainingText = content.substring(lastEnd);
        if (!remainingText.trim().isEmpty()) {
            parent.setOwnText(parent.ownText() + remainingText);
        }
    }

    private int findClosingTag(String content, int start, String closingTag) {
        int index = content.indexOf(closingTag, start);
        return index;
    }

    private void parseAttributes(Element element, String attrsString) {
        Matcher attrMatcher = ATTR_PATTERN.matcher(attrsString);
        while(attrMatcher.find()) {
            String key = attrMatcher.group(1);
            String value = attrMatcher.group(2);
            element.attributes().put(key, value);
        }
    }
}