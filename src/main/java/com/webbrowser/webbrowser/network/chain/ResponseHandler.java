package com.webbrowser.webbrowser.network.chain;


import com.webbrowser.webbrowser.network.HttpResponse;

public interface ResponseHandler {
    void setNextHandler(ResponseHandler nextHandler);
    HttpResponse handle(HttpResponse response);
}