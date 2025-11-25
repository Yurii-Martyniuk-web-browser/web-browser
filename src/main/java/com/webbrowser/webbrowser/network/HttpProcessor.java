package com.webbrowser.webbrowser.network;

import com.webbrowser.webbrowser.network.chain.ErrorHandler;
import com.webbrowser.webbrowser.network.chain.RedirectHandler;
import com.webbrowser.webbrowser.network.chain.ResponseHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpProcessor implements ResourceLoader {

    private final HttpClient httpClient;
    private final ResponseHandler chainStart;
    private final ExecutorService executor;


    public HttpProcessor() {
        this.httpClient = new HttpClient();

        RedirectHandler redirectHandler = new RedirectHandler(httpClient);
        ResponseHandler errorHandler = new ErrorHandler();
        redirectHandler.setNextHandler(errorHandler);

        this.chainStart = redirectHandler;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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

    @Override
    public byte[] loadResource(String url) {
        try {
            HttpRequest request = HttpRequest.createGet(url);
            HttpResponse response = httpClient.sendRequest(request);

            if (response.isSuccessful()) {
                return response.getBodyBytes();
            } else {
                System.err.println("Failed to load resource " + url + ". Status: " + response.getStatusCode());
                return new byte[0];
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid URL for resource: " + url);
            return new byte[0];
        } catch (java.io.IOException e) {
            System.err.println("Error loading resource " + url + ": " + e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public CompletableFuture<byte[]> loadResourceAsync(String url) {
        return CompletableFuture.supplyAsync(() -> loadResource(url), executor);
    }
}