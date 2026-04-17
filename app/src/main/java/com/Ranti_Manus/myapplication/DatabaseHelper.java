package com.Ranti_Manus.myapplication;

import android.os.Handler;
import android.os.Looper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper {

    // Executor for network requests to guarantee they do not block the main UI thread
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final Object schemaLock = new Object();
    private static volatile boolean schemaReady = false;

    public interface DbCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public static Connection getConnection() throws Exception {
        if (BuildConfig.DB_URL.isEmpty() || BuildConfig.DB_USER.isEmpty()) {
            throw new IllegalStateException("Database settings are missing. Add db.url, db.user, and db.pass to local.properties.");
        }

        // Connector/J 8 uses com.mysql.cj.jdbc.Driver, while 5.1.x uses com.mysql.jdbc.Driver.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Class.forName("com.mysql.jdbc.Driver");
        }

        Connection conn = DriverManager.getConnection(BuildConfig.DB_URL, BuildConfig.DB_USER, BuildConfig.DB_PASS);
        ensureSchema(conn);
        return conn;
    }

    private static void ensureSchema(Connection conn) throws SQLException {
        if (schemaReady) {
            return;
        }

        synchronized (schemaLock) {
            if (schemaReady) {
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                        "uid VARCHAR(100) PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "mobile VARCHAR(20), " +
                        "gender VARCHAR(10), " +
                        "blood_group VARCHAR(10), " +
                        "city VARCHAR(100), " +
                        "district VARCHAR(100), " +
                        "role ENUM('user','admin') NOT NULL DEFAULT 'user')");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS donors (" +
                        "uid VARCHAR(100) PRIMARY KEY, " +
                        "name VARCHAR(100), " +
                        "blood_group VARCHAR(10), " +
                        "city VARCHAR(100), " +
                        "mobile VARCHAR(20), " +
                        "last_donation_date DATE, " +
                        "is_available BOOLEAN NOT NULL DEFAULT TRUE)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS blood_requests (" +
                        "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "uid VARCHAR(100), " +
                        "name VARCHAR(100), " +
                        "mobile VARCHAR(20), " +
                        "city VARCHAR(100), " +
                        "blood_group VARCHAR(10), " +
                        "quantity INT NOT NULL DEFAULT 1, " +
                        "status ENUM('pending','approved','rejected','completed') NOT NULL DEFAULT 'pending', " +
                        "is_emergency BOOLEAN NOT NULL DEFAULT FALSE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "approved_at TIMESTAMP NULL, " +
                        "completed_at TIMESTAMP NULL)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS donations (" +
                        "donation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "donor_uid VARCHAR(100), " +
                        "blood_group VARCHAR(10) NOT NULL, " +
                        "quantity INT NOT NULL, " +
                        "donation_date DATE NOT NULL, " +
                        "expiry_date DATE NOT NULL, " +
                        "status ENUM('scheduled','completed','cancelled') NOT NULL DEFAULT 'completed')");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS blood_inventory (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "blood_group VARCHAR(10) NOT NULL, " +
                        "quantity INT NOT NULL, " +
                        "expiry_date DATE NOT NULL, " +
                        "source VARCHAR(30) NOT NULL DEFAULT 'manual', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS admin_logs (" +
                        "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "admin_uid VARCHAR(100), " +
                        "action VARCHAR(255) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            }

            addColumnIfMissing(conn, "users", "role", "role ENUM('user','admin') NOT NULL DEFAULT 'user'");
            addColumnIfMissing(conn, "donors", "last_donation_date", "last_donation_date DATE");
            addColumnIfMissing(conn, "donors", "is_available", "is_available BOOLEAN NOT NULL DEFAULT TRUE");
            addColumnIfMissing(conn, "blood_requests", "city", "city VARCHAR(100)");
            addColumnIfMissing(conn, "blood_requests", "status", "status ENUM('pending','approved','rejected','completed') NOT NULL DEFAULT 'pending'");
            addColumnIfMissing(conn, "blood_requests", "is_emergency", "is_emergency BOOLEAN NOT NULL DEFAULT FALSE");
            addColumnIfMissing(conn, "blood_requests", "approved_at", "approved_at TIMESTAMP NULL");
            addColumnIfMissing(conn, "blood_requests", "completed_at", "completed_at TIMESTAMP NULL");
            addColumnIfMissing(conn, "blood_inventory", "expiry_date", "expiry_date DATE NULL");
            addColumnIfMissing(conn, "blood_inventory", "source", "source VARCHAR(30) NOT NULL DEFAULT 'manual'");
            addColumnIfMissing(conn, "blood_inventory", "created_at", "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            addColumnIfMissing(conn, "donations", "expiry_date", "expiry_date DATE NULL");
            addColumnIfMissing(conn, "donations", "status", "status ENUM('scheduled','completed','cancelled') NOT NULL DEFAULT 'completed'");
            migrateLegacyBloodData(conn);

            schemaReady = true;
        }
    }

    private static void addColumnIfMissing(Connection conn, String tableName, String columnName, String columnDefinition) throws SQLException {
        if (columnExists(conn, tableName, columnName)) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnDefinition);
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?")) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?")) {
            pstmt.setString(1, tableName);
            pstmt.setString(2, columnName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static void migrateLegacyBloodData(Connection conn) throws SQLException {
        if (!tableExists(conn, "blood_data") || !columnExists(conn, "blood_data", "blood_group") || !columnExists(conn, "blood_data", "quantity")) {
            return;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM blood_inventory WHERE source='legacy'")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO blood_inventory (blood_group, quantity, expiry_date, source) " +
                    "SELECT blood_group, quantity, DATE_ADD(CURDATE(), INTERVAL 42 DAY), 'legacy' " +
                    "FROM blood_data WHERE quantity > 0");
        }
    }

    /**
     * Helper to run queries that don't return data (INSERT, UPDATE, DELETE).
     */
    public static void executeUpdate(String query, DbCallback<Boolean> callback) {
        executor.execute(() -> {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                int rows = stmt.executeUpdate(query);
                uiHandler.post(() -> callback.onSuccess(rows > 0));
                
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public interface PreparedStatementAction {
        void setParameters(java.sql.PreparedStatement pstmt) throws java.sql.SQLException;
    }

    public static void executeUpdate(String query, PreparedStatementAction action, DbCallback<Boolean> callback) {
        executor.execute(() -> {
            try (Connection conn = getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                if (action != null) {
                    action.setParameters(pstmt);
                }
                int rows = pstmt.executeUpdate();
                uiHandler.post(() -> callback.onSuccess(rows > 0));
                
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public interface ResultSetAction<T> {
        T process(java.sql.ResultSet rs) throws java.sql.SQLException;
    }

    public interface TransactionAction<T> {
        T execute(Connection conn) throws Exception;
    }

    public static <T> void executeQuery(String query, PreparedStatementAction action, ResultSetAction<T> rsAction, DbCallback<T> callback) {
        executor.execute(() -> {
            try (Connection conn = getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {

                if (action != null) {
                    action.setParameters(pstmt);
                }
                
                try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                    T result = rsAction.process(rs);
                    uiHandler.post(() -> callback.onSuccess(result));
                }

            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public static <T> void executeTransaction(TransactionAction<T> action, DbCallback<T> callback) {
        executor.execute(() -> {
            Connection conn = null;
            try {
                conn = getConnection();
                conn.setAutoCommit(false);
                T result = action.execute(conn);
                conn.commit();
                T finalResult = result;
                uiHandler.post(() -> callback.onSuccess(finalResult));
            } catch (Exception e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (Exception rollbackError) {
                        rollbackError.printStackTrace();
                    }
                }
                e.printStackTrace();
                uiHandler.post(() -> callback.onFailure(e));
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (Exception closeError) {
                        closeError.printStackTrace();
                    }
                }
            }
        });
    }
}
