package com.webbrowser.webbrowser.browser.rendering;

import com.webbrowser.webbrowser.browser.rendering.css.CssParser;
import com.webbrowser.webbrowser.browser.rendering.css.CssRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageContext {
    private final List<CssRule> cssRules = new ArrayList<>();
    private final Map<String, byte[]> images = new HashMap<>();

    public void addCssRules(String cssText) {
        this.cssRules.addAll(CssParser.parseRules(cssText));
        this.cssRules.sort(null);
    }

    public List<CssRule> getCssRules() {
        return cssRules;
    }

    public void addImage(String src, byte[] data) {
        images.put(src, data);
    }

    public byte[] getImage(String src) {
        return images.get(src);
    }

    public Map<String, byte[]> getImages() {
        return images;
    }
}