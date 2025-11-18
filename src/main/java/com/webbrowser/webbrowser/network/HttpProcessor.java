package com.webbrowser.webbrowser.network;

import com.webbrowser.webbrowser.network.chain.ErrorHandler;
import com.webbrowser.webbrowser.network.chain.RedirectHandler;
import com.webbrowser.webbrowser.network.chain.ResponseHandler;

import java.io.IOException;

public class HttpProcessor {
    private final HttpClient httpClient;
    private final ResponseHandler chainStart;

    public HttpProcessor() {
        this.httpClient = new HttpClient();

        ResponseHandler redirectHandler = new RedirectHandler(httpClient);
        ResponseHandler errorHandler = new ErrorHandler();

        redirectHandler.setNextHandler(errorHandler);

        this.chainStart = redirectHandler;
    }

    public HttpResponse loadUrl(String url) throws IllegalArgumentException {
        try {
            HttpRequest request = HttpRequest.createGet(url);

            HttpResponse initialResponse = httpClient.sendRequest(request);

            return chainStart.handle(initialResponse);

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid URL provided: " + url + " - " + e.getMessage());
            return HttpResponse.badRequest("The provided URL format is invalid.");

        } catch (IOException e) {
            System.err.println("Unexpected fatal load error: " + e.getMessage());
            return HttpResponse.internalError("An unexpected system error occurred during page loading.");
        }
    }
}