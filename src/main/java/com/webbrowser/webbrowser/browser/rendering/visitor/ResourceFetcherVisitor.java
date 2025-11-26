package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.PageContext; // Ваш новий клас контексту
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.network.ResourceLoader;
import com.webbrowser.webbrowser.network.UrlResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceFetcherVisitor implements NodeVisitor {

    private static final Logger logger = Logger.getLogger(ResourceFetcherVisitor.class.getName());

    private final ResourceLoader resourceLoader;
    private final String baseUrl;
    private final PageContext pageContext;

    private final Map<String, String> loadedScripts = new HashMap<>();

    private int inlineScriptCounter = 0;
    private final List<CompletableFuture<?>> pendingTasks = new ArrayList<>();

    public ResourceFetcherVisitor(ResourceLoader resourceLoader, String baseUrl, PageContext pageContext) {
        this.resourceLoader = resourceLoader;
        this.baseUrl = baseUrl;
        this.pageContext = pageContext;
    }

    public Map<String, String> getLoadedScripts() {
        return loadedScripts;
    }

    public void awaitCompletion() {
        if (!pendingTasks.isEmpty()) {
            CompletableFuture.allOf(pendingTasks.toArray(new CompletableFuture<?>[0]))
                    .join();
        }
    }

    @Override
    public void head(Node node) {
        if (!(node instanceof Element element)) {
            return;
        }

        String tagName = element.tagName().toLowerCase();

        if (tagName.equals("link") && "stylesheet".equalsIgnoreCase(element.getAttribute("rel"))) {
            String href = element.getAttribute("href");
            if (!href.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, href);
                    logger.info("Fetching CSS: " + absoluteUrl);

                    CompletableFuture<byte[]> future = resourceLoader.loadResourceAsync(absoluteUrl);
                    pendingTasks.add(future);

                    future.thenAccept(bytes -> {
                        if (bytes != null && bytes.length > 0) {
                            String css = new String(bytes);
                            synchronized (pageContext) {
                                pageContext.addCssRules(css);
                            }
                            logger.info("Loaded async css: " + absoluteUrl);
                        } else {
                            logger.warning("Failed to load async css: " + absoluteUrl);
                        }
                    });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to load CSS: " + href, e);
                }
            }
        }
        else if (tagName.equals("img")) {
            String src = element.getAttribute("src");
            if (!src.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, src);

                    CompletableFuture<byte[]> future = resourceLoader.loadResourceAsync(absoluteUrl);
                    pendingTasks.add(future);

                    future.thenAccept(bytes -> {
                        if (bytes != null && bytes.length > 0) {
                            synchronized (pageContext) {
                                pageContext.addImage(absoluteUrl, bytes);
                            }
                            element.attributes().put("src", absoluteUrl);
                            logger.info("Loaded async image: " + absoluteUrl);
                        } else {
                            logger.warning("Failed to load async image: " + absoluteUrl);
                        }
                    });
                } catch (Exception ignored) {
                }
            }
        }
        else if (tagName.equals("script")) {
            String src = element.getAttribute("src");

            if (src != null && !src.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, src);
                    logger.info("Fetching JS: " + absoluteUrl);

                    CompletableFuture<byte[]> future = resourceLoader.loadResourceAsync(absoluteUrl);
                    pendingTasks.add(future);

                    future.thenAccept(bytes -> {
                        if (bytes != null && bytes.length > 0) {
                            synchronized (loadedScripts) {
                                loadedScripts.put(absoluteUrl, new String(bytes));
                            }
                            element.attributes().put("src", absoluteUrl);
                            logger.info("Loaded async script: " + absoluteUrl);
                        }
                    });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to load JS: " + src, e);
                }
            } else {
                String inlineCode = element.text();
                if (!inlineCode.isEmpty()) {
                    String type = element.getAttribute("type");
                    if (type == null || type.isEmpty() || type.equals("text/javascript") || type.equals("application/javascript")) {
                        loadedScripts.put("Inline Script #" + (++inlineScriptCounter), inlineCode);
                    }
                }
            }
        }
        else if (tagName.equals("style")) {
            String cssContent = element.text();
            if (cssContent != null && !cssContent.isBlank()) {
                pageContext.addCssRules(cssContent);
            }
        }
    }

    @Override
    public void tail(Node node) {}
}