package com.webbrowser.webbrowser.browser.rendering;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class HtmlParser {

    public Document parse(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return Jsoup.parse("<html><head><title>Empty Content</title></head><body></body></html>");
        }

        return Jsoup.parse(htmlContent);
    }
}