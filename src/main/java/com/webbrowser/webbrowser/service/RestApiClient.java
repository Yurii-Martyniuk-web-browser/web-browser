package com.webbrowser.webbrowser.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.webbrowser.webbrowser.config.ApiConfig;
import com.webbrowser.webbrowser.dto.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RestApiClient {
    // Змініть порт/назву файлу відповідно до вашого деплою
    private static final String BASE_URL = ApiConfig.getApiUrl();

    private final HttpClient client;
    private final Gson gson;

    public RestApiClient() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // --- AUTH ---
    public CompletableFuture<AuthResponse> login(String email, String password) {
        return sendPostRequest("/auth/login", new AuthRequest(email, password), AuthResponse.class);
    }

    public CompletableFuture<AuthResponse> register(String email, String password) {
        return sendPostRequest("/auth/register", new AuthRequest(email, password), AuthResponse.class);
    }

    // --- HISTORY ---
    public void saveVisitAsync(String url, String title) {
        if (!UserSession.getInstance().isLoggedIn()) return;

        HistoryRequest req = new HistoryRequest(UserSession.getInstance().getUserId(), url, title);
        // "Fire and forget" - не чекаємо відповіді
        sendPostRequest("/history", req, Object.class);
    }

    public CompletableFuture<List<HistoryItemDto>> getHistory() {
        if (!UserSession.getInstance().isLoggedIn()) return CompletableFuture.completedFuture(List.of());

        String endpoint = "/history?userId=" + UserSession.getInstance().getUserId();
        return sendGetRequest(endpoint)
                .thenApply(json -> gson.fromJson(json, new TypeToken<List<HistoryItemDto>>(){}.getType()));
    }

    // --- RESOURCES ---
    public CompletableFuture<SnapshotResponse> getSnapshot(Long historyId) {
        return sendGetRequest("/resources/snapshot/" + historyId)
                .thenApply(json -> gson.fromJson(json, SnapshotResponse.class));
    }

    // --- HELPERS ---
    private <T> CompletableFuture<T> sendPostRequest(String endpoint, Object body, Class<T> responseType) {
        String jsonBody = gson.toJson(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, responseType));
    }

    private CompletableFuture<String> sendGetRequest(String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}