package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.CssStorage;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.network.ResourceLoader;
import com.webbrowser.webbrowser.network.UrlResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ResourceFetcherVisitor implements NodeVisitor {

    private final ResourceLoader resourceLoader;
    private final String baseUrl;
    private final Map<String, String> loadedScripts = new HashMap<>();
    private final Map<String, byte[]> loadedImages = new HashMap<>();
    private int inlineScriptCounter = 0;

    private final List<CompletableFuture<?>> pendingTasks = new ArrayList<>();

    public ResourceFetcherVisitor(ResourceLoader resourceLoader, String baseUrl) {
        this.resourceLoader = resourceLoader;
        this.baseUrl = baseUrl;
    }

    public Map<String, String> getLoadedScripts() {
        return loadedScripts;
    }

    public Map<String, byte[]> getLoadedImages() {
        CompletableFuture.allOf(pendingTasks.toArray(new CompletableFuture<?>[0]))
                .join();

        return loadedImages;
    }

    @Override
    public void head(Node node) {
        if (!(node instanceof Element element)) {
            return;
        }

        String tagName = element.tagName().toLowerCase();

        System.out.println("tagName: " + tagName);
        System.out.println("attr: " + element.attr("rel"));
        System.out.println("Attributes: " + element.attributes());

        if (tagName.equals("link") && "stylesheet".equalsIgnoreCase(element.getAttribute("rel"))) {
            String href = element.getAttribute("href");
            if (!href.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, href);
                    System.out.println("Fetching CSS: " + absoluteUrl);
                    CompletableFuture<byte[]> future = resourceLoader.loadResourceAsync(absoluteUrl);

                    pendingTasks.add(future);

                    future.thenAccept(bytes -> {
                                if (bytes != null && bytes.length > 0) {
                                    CssStorage.addGlobalStyles(new String(bytes));
                                    System.out.println("Loaded async css: " + absoluteUrl);
                                } else  {
                                    System.out.println("Failed to load async css: " + absoluteUrl);
                                }
                            });
                } catch (Exception e) {
                    System.err.println("Failed to load CSS: " + href);
                }
            }
        }
        else if (tagName.equals("img")) {
            String src = element.getAttribute("src");
            System.out.println("src in fetcher: " + src);
            if (!src.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, src);
                    CompletableFuture<byte[]> future = resourceLoader.loadResourceAsync(absoluteUrl);

                    pendingTasks.add(future);

                    future.thenAccept(bytes -> {
                                if (bytes != null && bytes.length > 0) {
                                    loadedImages.put(absoluteUrl, bytes);
                                    element.attributes().put("src", absoluteUrl);
                                    System.out.println("Loaded async image: " + absoluteUrl);
                                } else  {
                                    System.out.println("Failed to load async image: " + absoluteUrl);
                                }
                            });
                } catch (Exception ignored) {}
            }
        }
        else if (tagName.equals("script")) {
            String src = element.getAttribute("src");
            System.out.println("Fetching script: " + src);

            if (src != null && !src.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, src);
                    System.out.println("Fetching JS: " + absoluteUrl);
                    CompletableFuture<byte[]> future = resourceLoader.loadResourceAsync(absoluteUrl);

                    pendingTasks.add(future);

                    future.thenAccept(bytes -> {
                                if (bytes != null && bytes.length > 0) {
                                    loadedScripts.put(absoluteUrl, new String(bytes));
                                    element.attributes().put("src", absoluteUrl);
                                    System.out.println("Loaded async script: " + absoluteUrl);
                                } else  {
                                    System.out.println("Failed to load async script: " + absoluteUrl);
                                }
                            });
                } catch (Exception e) {
                    System.err.println("Failed to load JS: " + src);
                }
            } else {
                String inlineCode = element.text();

                System.out.println("--- Processing Inline Script ---");
                System.out.println("Raw text inside script tag: '" + inlineCode + "'");

                if (!inlineCode.isEmpty()) {
                    String type = element.getAttribute("type");
                    if (type == null || type.isEmpty() || type.equals("text/javascript") || type.equals("application/javascript")) {
                        loadedScripts.put("Inline Script #" + (++inlineScriptCounter), inlineCode);
                    } else {
                        System.out.println("Skipping script with type: " + type);
                    }
                }
            }
        }
    }

    @Override
    public void tail(Node node) {}
}