package com.webbrowser.webbrowser.network.chain;

import com.webbrowser.webbrowser.network.HttpResponse;

abstract class AbstractResponseHandler implements ResponseHandler {
    protected ResponseHandler nextHandler;

    @Override
    public void setNextHandler(ResponseHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    protected HttpResponse passToNext(HttpResponse response) {
        if (nextHandler != null) {
            return nextHandler.handle(response);
        }
        return response;
    }
}