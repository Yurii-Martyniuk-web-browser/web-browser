package com.webbrowser.webbrowser.browser.rendering.css;

import java.util.Map;

public class CssRule implements Comparable<CssRule> {
    public final String selector;
    public final Map<String, String> properties;
    public final int specificity;

    public CssRule(String selector, Map<String, String> properties) {
        this.selector = selector;
        this.properties = properties;
        this.specificity = calculateSpecificity(selector);
    }

    private int calculateSpecificity(String selector) {
        if (selector.startsWith("#")) return 100;
        if (selector.startsWith(".")) return 10;
        return 1;
    }

    @Override
    public int compareTo(CssRule other) {
        return Integer.compare(this.specificity, other.specificity);
    }
}