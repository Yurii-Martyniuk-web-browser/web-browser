package com.webbrowser.webbrowser.browser.core;

import com.webbrowser.webbrowser.browser.rendering.DomTraverser;
import com.webbrowser.webbrowser.browser.rendering.dom.Document;
import com.webbrowser.webbrowser.browser.rendering.visitor.ResourceFetcherVisitor;
import com.webbrowser.webbrowser.network.HttpProcessor;

class ResourceLoadingService {

    public ResourceFetcherVisitor collectResources(Document doc, String baseUrl) {

        ResourceFetcherVisitor visitor =
                new ResourceFetcherVisitor(new HttpProcessor(), baseUrl);

        DomTraverser.traverse(doc.getRoot(), visitor);

        return visitor;
    }
}
