package com.Ranti_Manus.myapplication;

import android.os.Handler;
import android.os.Looper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper {

    // Executor for network requests to guarantee they do not block the main UI thread
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

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

        return DriverManager.getConnection(BuildConfig.DB_URL, BuildConfig.DB_USER, BuildConfig.DB_PASS);
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
}
