package com.webbrowser.webbrowser.browser.rendering.visitor;

import com.webbrowser.webbrowser.browser.rendering.CssStorage;
import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.dom.Node;
import com.webbrowser.webbrowser.network.ResourceLoader;
import com.webbrowser.webbrowser.network.UrlResolver;

import java.util.HashMap;
import java.util.Map;

public class ResourceFetcherVisitor implements NodeVisitor {

    private final ResourceLoader resourceLoader;
    private final String baseUrl;
    private final Map<String, String> loadedScripts = new HashMap<>();
    private int inlineScriptCounter = 0;

    public ResourceFetcherVisitor(ResourceLoader resourceLoader, String baseUrl) {
        this.resourceLoader = resourceLoader;
        this.baseUrl = baseUrl;
    }

    public Map<String, String> getLoadedScripts() {
        return loadedScripts;
    }

    @Override
    public void head(Node node) {
        // 1. ВАЖЛИВО: Перевіряємо тип вузла.
        // Ми шукаємо теги <link>, <img>, <script>, тому TextNode нас тут не цікавлять.
        if (!(node instanceof Element)) {
            return;
        }

        Element element = (Element) node;
        String tagName = element.tagName().toLowerCase();

        // 1. Обробка CSS
        if (tagName.equals("link") && "stylesheet".equalsIgnoreCase(element.attr("rel"))) {
            String href = element.attr("href");
            if (!href.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, href);
                    System.out.println("Fetching CSS: " + absoluteUrl);
                    String cssText = resourceLoader.loadResource(absoluteUrl);
                    if (!cssText.isEmpty()) {
                        CssStorage.addGlobalStyles(cssText);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load CSS: " + href);
                }
            }
        }
        // 2. Обробка Картинок
        else if (tagName.equals("img")) {
            String src = element.attr("src");
            if (!src.isEmpty()) {
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, src);
                    element.attributes().put("src", absoluteUrl);
                } catch (Exception ignored) {}
            }
        }
        // 3. Збір JS коду
        else if (tagName.equals("script")) {
            String src = element.attr("src");

            if (!src.isEmpty()) {
                // Зовнішній скрипт (src="...")
                try {
                    String absoluteUrl = UrlResolver.resolve(baseUrl, src);
                    System.out.println("Fetching JS: " + absoluteUrl);
                    String jsCode = resourceLoader.loadResource(absoluteUrl);
                    if (!jsCode.isEmpty()) {
                        loadedScripts.put(absoluteUrl, jsCode);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load JS: " + src);
                }
            } else {
                // Інлайн скрипт (<script>code...</script>)
                // ТУТ ЗМІНА: Використовуємо element.text(), який склеює дітей-TextNodes
                String inlineCode = element.text();

                if (!inlineCode.isEmpty()) {
                    loadedScripts.put("Inline Script #" + (++inlineScriptCounter), inlineCode);
                }
            }
        }
    }

    @Override
    public void tail(Node node) {}
}