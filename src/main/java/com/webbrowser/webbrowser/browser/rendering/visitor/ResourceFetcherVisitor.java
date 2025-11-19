package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.CssStorage;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.network.ResourceLoader;

public class ResourceFetcherVisitor implements NodeVisitor {

    private final ResourceLoader resourceLoader;

    public ResourceFetcherVisitor(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void head(Element element) {
        String tagName = element.tagName().toLowerCase();


        if (tagName.equals("link") && element.attr("rel").equalsIgnoreCase("stylesheet")) {
            String href = element.attr("href");
            if (!href.isEmpty()) {
                System.out.println("DEBUG: Fetching external CSS stylesheet: " + href);
                String cssText = resourceLoader.loadResource(href);

                if (!cssText.isEmpty()) {
                    CssStorage.addGlobalStyles(cssText);
                    System.out.println("DEBUG: Successfully loaded and stored external CSS.");
                }
            }
        } else if (tagName.equals("img")) {
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