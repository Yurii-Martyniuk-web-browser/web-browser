package com.webbrowser.webbrowser.dto;

public record HistoryRequest(
        Long userId,
        String url,
        String title
) {}