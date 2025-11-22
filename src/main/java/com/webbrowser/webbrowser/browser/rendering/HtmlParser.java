package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.TextNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {
    private static final Set<String> VOID_TAGS = Set.of(
            "br", "img", "hr", "meta", "link", "input", "source", "track", "wbr",
            "area", "base", "col", "embed", "param"
    );

    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9\\-]+)([^>]*)>");
    private static final Pattern ATTR_PATTERN = Pattern.compile("([a-zA-Z0-9\\-]+)=\\\"([^\\\"]*)\\\"");

    public Document parse(String html) {
        String cleanHtml = preprocessHtml(html);

        Element root = new Element("html");
        Document doc = new Document(root);
        List<Token> tokens = tokenize(cleanHtml);
        parseTokens(tokens, root, doc);
        return doc;
    }

    private String preprocessHtml(String html) {
        html = html.replaceAll("<!(?i)DOCTYPE[^>]*>", "");

//        html = html.replaceAll("(?i)<script[\\s\\S]*?</script>", "");
//        html = html.replaceAll("(?i)<style[\\s\\S]*?</style>", "");

        return html;
    }

    private static class Token {
        enum Type { OPEN, CLOSE, TEXT, VOID }
        Type type;
        String name;
        String attrs;
        String text;
    }

    private List<Token> tokenize(String html) {
        List<Token> tokens = new ArrayList<>();
        Matcher m = TAG_PATTERN.matcher(html);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                String text = html.substring(last, m.start());
                if (!text.isBlank()) {
                    Token t = new Token();
                    t.type = Token.Type.TEXT;
                    t.text = text;
                    tokens.add(t);
                }
            }
            String slash = m.group(1);
            String name = m.group(2).toLowerCase();
            String attrs = m.group(3);
            Token t = new Token();

            if (!slash.isEmpty()) {
                t.type = Token.Type.CLOSE;
                t.name = name;
            } else if (VOID_TAGS.contains(name)) {
                t.type = Token.Type.VOID;
                t.name = name;
                t.attrs = attrs;
            } else {
                t.type = Token.Type.OPEN;
                t.name = name;
                t.attrs = attrs;
            }
            tokens.add(t);
            last = m.end();
        }
        if (last < html.length()) {
            String remainder = html.substring(last);
            if (!remainder.isBlank()) {
                Token t = new Token();
                t.type = Token.Type.TEXT;
                t.text = remainder;
                tokens.add(t);
            }
        }
        return tokens;
    }

    private void parseTokens(List<Token> tokens, Element root, Document doc) {
        Deque<Element> stack = new ArrayDeque<>();
        stack.push(root);

        for (Token t : tokens) {
            switch (t.type) {
                case TEXT -> {
                    String cleanText = t.text.replaceAll("\\s+", " ");
                    if (!cleanText.isBlank()) {
                        stack.peek().addChild(new TextNode(cleanText));
                    }
                }
                case VOID -> {
                    Element e = new Element(t.name);
                    parseAttributes(e, t.attrs);
                    stack.peek().addChild(e);
                }
                case OPEN -> {
                    Element e = new Element(t.name);
                    parseAttributes(e, t.attrs);
                    stack.peek().addChild(e);
                    if (!t.name.equals("link") && !t.name.equals("meta")) {
                        stack.push(e);
                    }
                }
                case CLOSE -> {
                    String closing = t.name;
                    if (closing.equals("title") && !stack.isEmpty() && stack.peek().tagName().equals("title")) {
                        doc.setTitle(stack.peek().text());
                        stack.pop();
                        continue;
                    }

                    if (stack.stream().anyMatch(e -> e.tagName().equals(closing))) {
                        while (!stack.isEmpty() && !stack.peek().tagName().equals(closing)) {
                            stack.pop();
                        }
                        if (!stack.isEmpty()) stack.pop();
                    }
                }
            }
        }
    }

    private void parseAttributes(Element e, String attrs) {
        if (attrs == null) return;
        Matcher m = ATTR_PATTERN.matcher(attrs);
        while (m.find()) e.attributes().put(m.group(1), m.group(2));
    }
}