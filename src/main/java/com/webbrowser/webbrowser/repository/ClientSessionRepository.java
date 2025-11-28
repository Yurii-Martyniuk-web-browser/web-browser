package com.webbrowser.webbrowser.repository;

import java.sql.*;
import java.util.logging.Logger;

public class ClientSessionRepository {

    private static final Logger log = Logger.getLogger(ClientSessionRepository.class.getName());
    private static final String DB_URL = "jdbc:sqlite:browser_client.db";

    private static final String SQL_INIT = """
        CREATE TABLE IF NOT EXISTS session (
            id INTEGER PRIMARY KEY CHECK (id = 1),\s
            user_id INTEGER,\s
            email TEXT
        );\s
        """;

    private static final String SQL_INSERT_OR_REPLACE = """
        INSERT OR REPLACE INTO session (id, user_id, email)\s
        VALUES (1, ?, ?);\s
        """;

    private static final String SQL_SELECT = """
        SELECT user_id, email\s
        FROM session\s
        WHERE id = 1;
       \s""";

    private static final String SQL_DELETE = """
        DELETE FROM session\s
        WHERE id = 1;
        """;


    public ClientSessionRepository() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(SQL_INIT);

        } catch (SQLException e) {
            log.warning("Error in creating database connection: " + e.getMessage());
        }
    }

    public void saveSession(Long userId, String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_OR_REPLACE)) {

            stmt.setLong(1, userId);
            stmt.setString(2, email);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log.warning("Error in saving session: " + e.getMessage());
        }
    }

    public SessionData loadSession() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT)) {

            if (rs.next()) {
                return new SessionData(rs.getLong("user_id"), rs.getString("email"));
            }

        } catch (SQLException e) {
            log.warning("Error in getting session: " + e.getMessage());
        }
        return null;
    }

    public record SessionData(Long userId, String email) {}
}
