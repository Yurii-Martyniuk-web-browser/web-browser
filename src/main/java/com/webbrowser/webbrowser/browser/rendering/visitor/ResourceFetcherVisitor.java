package com.webbrowser.webbrowser.browser.rendering.visitor;

import org.jsoup.nodes.Element;

public class ResourceFetcherVisitor implements NodeVisitor {

    @Override
    public void head(Element element) {
        String tagName = element.tagName().toLowerCase();

        if (tagName.equals("img")) {
            String src = element.attr("src");
            if (!src.isEmpty()) {
                System.out.println("DEBUG: Fetching image resource: " + src);
            }
        }
        else if (tagName.equals("link") && element.attr("rel").equalsIgnoreCase("stylesheet")) {
            String href = element.attr("href");
            if (!href.isEmpty()) {
                System.out.println("DEBUG: Fetching CSS stylesheet: " + href);
            }
        }
        else if (tagName.equals("script")) {
            String src = element.attr("src");
            if (!src.isEmpty()) {
                System.out.println("DEBUG: Fetching JS script: " + src);
            }
        }
    }

    @Override
    public void tail(Element element) {
    }
}