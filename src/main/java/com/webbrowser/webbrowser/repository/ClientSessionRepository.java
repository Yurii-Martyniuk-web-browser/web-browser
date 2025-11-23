package com.webbrowser.webbrowser.repository;

import java.sql.*;

public class ClientSessionRepository {

    private static final String DB_URL = "jdbc:sqlite:browser_client.db";

    public ClientSessionRepository() {
        initDatabase();
    }

    // 1. Ініціалізація таблиці при першому запуску
    private void initDatabase() {
        // Створюємо таблицю, де буде лише один рядок (id = 1)
        String sql = """
            CREATE TABLE IF NOT EXISTS session (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                user_id INTEGER,
                email TEXT
            );
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Помилка ініціалізації локальної БД: " + e.getMessage());
        }
    }

    // 2. Збереження сесії (Login)
    public void saveSession(Long userId, String email) {
        // INSERT OR REPLACE - якщо запис є, він оновиться, якщо ні - створиться
        String sql = "INSERT OR REPLACE INTO session (id, user_id, email) VALUES (1, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, email);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Отримання сесії (Auto-login при запуску)
    public SessionData loadSession() {
        String sql = "SELECT user_id, email FROM session WHERE id = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new SessionData(rs.getLong("user_id"), rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Сесії немає
    }

    // 4. Видалення сесії (Logout)
    public void clearSession() {
        String sql = "DELETE FROM session WHERE id = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Простий DTO для повернення даних
    public record SessionData(Long userId, String email) {}
}