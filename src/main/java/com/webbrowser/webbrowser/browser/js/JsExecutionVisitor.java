package com.webbrowser.webbrowser.browser.js;

import com.webbrowser.webbrowser.browser.rendering.dom.Element;
import com.webbrowser.webbrowser.browser.rendering.visitor.NodeVisitor;
import com.webbrowser.webbrowser.network.ResourceLoader;


public class JsExecutionVisitor implements NodeVisitor {

    private final JsEngine jsEngine;
    private final ResourceLoader resourceLoader;

    public JsExecutionVisitor(JsEngine jsEngine, ResourceLoader resourceLoader) {
        this.jsEngine = jsEngine;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void head(Element element) {
        if (element.tagName().equals("script")) {
            String src = element.attr("src");

            if (!src.isEmpty()) {
                String jsCode = resourceLoader.loadResource(src);
                if (!jsCode.isEmpty()) {
                    jsEngine.execute(jsCode);
                }
            } else {
                String scriptContent = element.text();

                if (!scriptContent.isEmpty()) {
                    jsEngine.execute(scriptContent);
                }
            }
        }
    }

    @Override
    public void tail(Element element) {
    }
}