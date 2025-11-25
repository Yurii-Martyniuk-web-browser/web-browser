package com.webbrowser.webbrowser.service;

import com.webbrowser.webbrowser.repository.ClientSessionRepository;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UserSession {

    private static final Logger log = Logger.getLogger(UserSession.class.getName());

    private static UserSession INSTANCE;
    private final ClientSessionRepository repository;

    private Long userId;
    private String email;

    private UserSession() {
        this.repository = new ClientSessionRepository();

        var savedSession = repository.loadSession();
        if (savedSession != null) {
            this.userId = savedSession.userId();
            this.email = savedSession.email();
            log.log(Level.INFO, "Session restored for user: {0}", this.email);
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
        repository.saveSession(userId, email);
    }

    public boolean isLoggedIn() {
        return userId != null;
    }

    public Long getUserId() {
        return userId;
    }
}