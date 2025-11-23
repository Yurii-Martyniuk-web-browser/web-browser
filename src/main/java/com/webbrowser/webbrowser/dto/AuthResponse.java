package com.webbrowser.webbrowser.dto;

public record AuthResponse(boolean success, Long userId, String message) {}