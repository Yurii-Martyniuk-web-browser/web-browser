package com.webbrowser.webbrowser.network.chain;

import com.webbrowser.webbrowser.network.HttpClient;
import com.webbrowser.webbrowser.network.HttpRequest;
import com.webbrowser.webbrowser.network.HttpResponse;

import java.io.IOException;

public class RedirectHandler extends AbstractResponseHandler {
    private final HttpClient httpClient;
    private int redirectCount = 0;
    private static final int MAX_REDIRECTS = 5;

    public RedirectHandler(HttpClient client) {
        this.httpClient = client;
    }

    public void resetRedirectCount() {
        this.redirectCount = 0;
    }

    @Override
    public HttpResponse handle(HttpResponse response) {
        if (response.isRedirect()) {

            if (redirectCount >= MAX_REDIRECTS) {
                return HttpResponse.internalError("Redirect loop detected. Exceeded " + MAX_REDIRECTS + " redirects.");
            }

            String newLocation = response.getHeader("Location");

            if (newLocation != null) {
                System.out.println("Redirecting to: " + newLocation);
                redirectCount++;
                try {
                    HttpRequest newRequest = HttpRequest.createGet(newLocation);
                    HttpResponse newResponse = httpClient.sendRequest(newRequest);

                    return handle(newResponse);
                } catch (IOException | IllegalArgumentException e) {
                    System.err.println("Redirect failed due to I/O or invalid URL: " + e.getMessage());
                    return HttpResponse.internalError("Error during redirect processing to " + newLocation + ": " + e.getMessage());
                }
            } else {
                System.err.println("Redirect response received without Location header.");
                return HttpResponse.internalError("Server sent a redirect code without a Location header.");
            }
        }

        return passToNext(response);
    }
}