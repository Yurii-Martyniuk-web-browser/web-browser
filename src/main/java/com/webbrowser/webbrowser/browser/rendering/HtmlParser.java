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

    private static final Pattern TAG_PATTERN =
            Pattern.compile("<(/?)([a-zA-Z0-9\\-]+)([^>]*)>");

    private static final Pattern ATTR_PATTERN =
            Pattern.compile("([a-zA-Z0-9\\-]+)=\"([^\"]*)\"");

    public Document parse(String html) {
        String cleanHtml = preprocessHtml(html);

        Element root = new Element("html");
        Document doc = new Document(root);

        List<Token> tokens = tokenize(cleanHtml);
        parseTokens(tokens, root, doc);

        return doc;
    }

    private String preprocessHtml(String html) {
        return html
                .replaceAll("<!(?i)DOCTYPE[^>]*>", "")
                .replaceAll("<!--[\\s\\S]*?-->", "");
    }

    private List<Token> tokenize(String html) {
        List<Token> tokens = new ArrayList<>();

        Matcher matcher = TAG_PATTERN.matcher(html);
        int last = 0;

        while (matcher.find()) {

            if (matcher.start() > last) {
                String text = html.substring(last, matcher.start());
                if (!text.isBlank()) {
                    Token t = new Token(Token.Type.TEXT);
                    t.text = text;
                    tokens.add(t);
                }
            }

            String slash = matcher.group(1);
            String name = matcher.group(2).toLowerCase();
            String attrs = matcher.group(3);

            Token t;

            if (!slash.isEmpty()) {
                t = new Token(Token.Type.CLOSE);
                t.name = name;

            } else if (VOID_TAGS.contains(name)) {
                t = new Token(Token.Type.VOID);
                t.name = name;
                t.attrs = attrs;

            } else {
                t = new Token(Token.Type.OPEN);
                t.name = name;
                t.attrs = attrs;
            }

            tokens.add(t);
            last = matcher.end();
        }

        if (last < html.length()) {
            String remainder = html.substring(last);
            if (!remainder.isBlank()) {
                Token t = new Token(Token.Type.TEXT);
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
                        Objects.requireNonNull(stack.peek()).addChild(new TextNode(cleanText));
                    }
                }

                case VOID -> {
                    Element e = new Element(t.name);
                    parseAttributes(e, t.attrs);
                    Objects.requireNonNull(stack.peek()).addChild(e);
                }

                case OPEN -> {
                    Element e = new Element(t.name);
                    parseAttributes(e, t.attrs);

                    Objects.requireNonNull(stack.peek()).addChild(e);

                    if (!t.name.equals("link") && !t.name.equals("meta")) {
                        stack.push(e);
                    }
                }

                case CLOSE -> {
                    String closing = t.name;

                    if (closing.equals("title")
                            && !stack.isEmpty()
                            && stack.peek().tagName().equals("title")) {

                        doc.setTitle(Objects.requireNonNull(stack.peek()).text());
                        stack.pop();
                        continue;
                    }

                    while (!stack.isEmpty()
                            && !stack.peek().tagName().equals(closing)) {
                        stack.pop();
                    }

                    if (!stack.isEmpty()) {
                        stack.pop();
                    }
                }
            }
        }
    }

    private void parseAttributes(Element e, String attrs) {
        if (attrs == null) return;

        Matcher m = ATTR_PATTERN.matcher(attrs);
        while (m.find()) {
            e.attributes().put(m.group(1), m.group(2));
        }
    }

    private static class Token {

        enum Type { OPEN, CLOSE, TEXT, VOID }

        final Type type;
        String name;
        String attrs;
        String text;

        Token(Type type) {
            this.type = type;
        }
    }
}
