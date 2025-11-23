package com.webbrowser.webbrowser.service;

import com.webbrowser.webbrowser.repository.ClientSessionRepository;

public class UserSession {
    private static UserSession INSTANCE;

    private final ClientSessionRepository repository;

    private Long userId;
    private String email;

    // Приватний конструктор
    private UserSession() {
        this.repository = new ClientSessionRepository();

        // Спроба автоматичного входу при запуску
        var savedSession = repository.loadSession();
        if (savedSession != null) {
            this.userId = savedSession.userId();
            this.email = savedSession.email();
            System.out.println("Відновлено сесію для: " + this.email);
        }
    }

    public static synchronized UserSession getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserSession();
        }
        return INSTANCE;
    }

    public void login(Long userId, String email) {
        this.userId = userId;
        this.email = email;
        // Зберігаємо в файл
        repository.saveSession(userId, email);
    }

    public void logout() {
        this.userId = null;
        this.email = null;
        // Видаляємо з файлу
        repository.clearSession();
    }

    public boolean isLoggedIn() {
        return userId != null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}