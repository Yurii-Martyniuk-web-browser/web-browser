package com.webbrowser.webbrowser.network.chain;

import com.webbrowser.webbrowser.network.HttpResponse;

public class ErrorHandler extends AbstractResponseHandler {

    @Override
    public HttpResponse handle(HttpResponse response) {
        if (response.isClientError() || response.isServerError()) {

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                return response;
            }


            final int statusCode = response.getStatusCode();
            final String statusText = response.getStatusText();

            if (response.isNotFound()) {
                return HttpResponse.notFound("Resource not specified or found.");
            } else if (response.isBadRequest()) {
                return HttpResponse.badRequest(statusText);
            } else if (response.isServiceUnavailable() || response.isBadGateway()) {
                return HttpResponse.serviceUnavailable(statusText);
            } else if (response.isServerError()) {
                return HttpResponse.internalError(statusText);
            } else if (response.isClientError()) {
                String reason = String.format("%d %s: Access Denied or Malformed Request.", statusCode, statusText);
                return HttpResponse.create(
                        statusCode,
                        statusText,
                        null,
                        String.format("<html><body><h1>%d %s</h1><p>The server responded with an error.</p></body></html>", statusCode, statusText)
                );
            }
            return response;
        }

        return passToNext(response);
    }
}