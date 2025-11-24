package com.webbrowser.webbrowser.network;

import java.util.concurrent.CompletableFuture;

public interface ResourceLoader {
    byte[] loadResource(String url);
    CompletableFuture<byte[]> loadResourceAsync(String url);
}