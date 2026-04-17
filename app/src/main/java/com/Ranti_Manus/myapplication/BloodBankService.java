package com.Ranti_Manus.myapplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class BloodBankService {
    private BloodBankService() {
    }

    public static class DashboardStats {
        public int totalUnits;
        public int pendingRequests;
        public int donorCount;
    }

    public static class InventorySummary {
        public String bloodGroup;
        public int quantity;

        public InventorySummary(String bloodGroup, int quantity) {
            this.bloodGroup = bloodGroup;
            this.quantity = quantity;
        }
    }

    public static class RequestItem {
        public int requestId;
        public String uid;
        public String name;
        public String mobile;
        public String city;
        public String bloodGroup;
        public int quantity;
        public String status;
        public boolean emergency;

        public String displayText() {
            String prefix = emergency ? "[EMERGENCY] " : "";
            return prefix + "#" + requestId + " " + bloodGroup + " - " + quantity + " units - " + status +
                    "\n" + name + " | " + mobile + " | " + city;
        }
    }

    public static class DonationItem {
        public String donorName;
        public String bloodGroup;
        public int quantity;
        public String donationDate;
        public String expiryDate;

        public String displayText() {
            return donorName + " donated " + quantity + " units of " + bloodGroup +
                    "\nDate: " + donationDate + " | Expiry: " + expiryDate;
        }
    }

    public static void loadDashboardStats(DatabaseHelper.DbCallback<DashboardStats> callback) {
        String query = "SELECT " +
                "(SELECT COALESCE(SUM(quantity),0) FROM blood_inventory WHERE expiry_date >= CURDATE()) AS total_units, " +
                "(SELECT COUNT(*) FROM blood_requests WHERE status='pending') AS pending_requests, " +
                "(SELECT COUNT(*) FROM donors WHERE is_available=TRUE) AS donor_count";
        DatabaseHelper.executeQuery(query, null, rs -> {
            DashboardStats stats = new DashboardStats();
            if (rs.next()) {
                stats.totalUnits = rs.getInt("total_units");
                stats.pendingRequests = rs.getInt("pending_requests");
                stats.donorCount = rs.getInt("donor_count");
            }
            return stats;
        }, callback);
    }

    public static void loadInventorySummary(DatabaseHelper.DbCallback<ArrayList<InventorySummary>> callback) {
        String query = "SELECT blood_group, COALESCE(SUM(quantity),0) AS total_units " +
                "FROM blood_inventory WHERE expiry_date >= CURDATE() AND quantity > 0 " +
                "GROUP BY blood_group ORDER BY blood_group";
        DatabaseHelper.executeQuery(query, null, rs -> {
            ArrayList<InventorySummary> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new InventorySummary(rs.getString("blood_group"), rs.getInt("total_units")));
            }
            return list;
        }, callback);
    }

    public static void findCompatibleDonors(String requestBloodGroup, String city, DatabaseHelper.DbCallback<ArrayList<Model_Donor>> callback) {
        String query = "SELECT uid, name, blood_group, city, mobile FROM donors WHERE city=? AND is_available=TRUE";
        DatabaseHelper.executeQuery(query, pstmt -> pstmt.setString(1, city), rs -> {
            ArrayList<Model_Donor> matches = new ArrayList<>();
            while (rs.next()) {
                String donorGroup = rs.getString("blood_group");
                if (BloodCompatibility.canDonateTo(donorGroup, requestBloodGroup)) {
                    matches.add(new Model_Donor(
                            rs.getString("uid"),
                            rs.getString("name"),
                            donorGroup,
                            rs.getString("city"),
                            rs.getString("mobile")
                    ));
                }
            }
            return matches;
        }, callback);
    }

    public static void loadRequests(DatabaseHelper.DbCallback<ArrayList<RequestItem>> callback) {
        String query = "SELECT request_id, uid, name, mobile, city, blood_group, quantity, status, is_emergency " +
                "FROM blood_requests ORDER BY is_emergency DESC, created_at DESC";
        DatabaseHelper.executeQuery(query, null, BloodBankService::readRequests, callback);
    }

    public static void loadRequestsForUser(String uid, DatabaseHelper.DbCallback<ArrayList<RequestItem>> callback) {
        String query = "SELECT request_id, uid, name, mobile, city, blood_group, quantity, status, is_emergency " +
                "FROM blood_requests WHERE uid=? ORDER BY is_emergency DESC, created_at DESC";
        DatabaseHelper.executeQuery(query, pstmt -> pstmt.setString(1, uid), BloodBankService::readRequests, callback);
    }

    public static void loadDonationHistory(DatabaseHelper.DbCallback<ArrayList<DonationItem>> callback) {
        String query = "SELECT COALESCE(d.name, donations.donor_uid) AS donor_name, donations.blood_group, donations.quantity, " +
                "donations.donation_date, donations.expiry_date FROM donations " +
                "LEFT JOIN donors d ON donations.donor_uid = d.uid ORDER BY donations.donation_date DESC";
        DatabaseHelper.executeQuery(query, null, rs -> {
            ArrayList<DonationItem> list = new ArrayList<>();
            while (rs.next()) {
                DonationItem item = new DonationItem();
                item.donorName = rs.getString("donor_name");
                item.bloodGroup = rs.getString("blood_group");
                item.quantity = rs.getInt("quantity");
                item.donationDate = rs.getString("donation_date");
                item.expiryDate = rs.getString("expiry_date");
                list.add(item);
            }
            return list;
        }, callback);
    }

    public static void updateRequestStatus(String adminUid, int requestId, String nextStatus, DatabaseHelper.DbCallback<Boolean> callback) {
        DatabaseHelper.executeTransaction(conn -> {
            RequestItem request = getRequestForUpdate(conn, requestId);
            if (request == null) {
                throw new SQLException("Request not found");
            }

            if ("approved".equals(nextStatus)) {
                if (!"pending".equals(request.status)) {
                    throw new SQLException("Only pending requests can be approved");
                }
                reduceInventory(conn, request.bloodGroup, request.quantity);
                updateRequestStatus(conn, requestId, "approved", "approved_at");
                logAdmin(conn, adminUid, "Approved request #" + requestId);
            } else if ("rejected".equals(nextStatus)) {
                if (!"pending".equals(request.status)) {
                    throw new SQLException("Only pending requests can be rejected");
                }
                updateRequestStatus(conn, requestId, "rejected", null);
                logAdmin(conn, adminUid, "Rejected request #" + requestId);
            } else if ("completed".equals(nextStatus)) {
                if (!"approved".equals(request.status)) {
                    throw new SQLException("Only approved requests can be completed");
                }
                updateRequestStatus(conn, requestId, "completed", "completed_at");
                logAdmin(conn, adminUid, "Completed request #" + requestId);
            } else {
                throw new SQLException("Invalid request status");
            }
            return true;
        }, callback);
    }

    public static void addManualInventory(String adminUid, String bloodGroup, int quantity, String expiryDate, DatabaseHelper.DbCallback<Boolean> callback) {
        DatabaseHelper.executeTransaction(conn -> {
            if (quantity <= 0) throw new SQLException("Quantity must be positive");
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO blood_inventory (blood_group, quantity, expiry_date, source) VALUES (?, ?, ?, 'manual')")) {
                pstmt.setString(1, bloodGroup);
                pstmt.setInt(2, quantity);
                pstmt.setString(3, expiryDate);
                pstmt.executeUpdate();
            }
            logAdmin(conn, adminUid, "Added " + quantity + " units of " + bloodGroup + " expiring on " + expiryDate);
            return true;
        }, callback);
    }

    public static void removeManualInventory(String adminUid, String bloodGroup, int quantity, DatabaseHelper.DbCallback<Boolean> callback) {
        DatabaseHelper.executeTransaction(conn -> {
            if (quantity <= 0) throw new SQLException("Quantity must be positive");
            reduceExactInventory(conn, bloodGroup, quantity);
            logAdmin(conn, adminUid, "Removed " + quantity + " units of " + bloodGroup);
            return true;
        }, callback);
    }

    public static int getAvailableCompatibleUnits(Connection conn, String requestBloodGroup) throws SQLException {
        int total = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT blood_group, quantity FROM blood_inventory WHERE expiry_date >= CURDATE() AND quantity > 0")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (BloodCompatibility.canDonateTo(rs.getString("blood_group"), requestBloodGroup)) {
                        total += rs.getInt("quantity");
                    }
                }
            }
        }
        return total;
    }

    public static void logAdmin(Connection conn, String adminUid, String action) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO admin_logs (admin_uid, action) VALUES (?, ?)")) {
            pstmt.setString(1, adminUid);
            pstmt.setString(2, action);
            pstmt.executeUpdate();
        }
    }

    private static ArrayList<RequestItem> readRequests(ResultSet rs) throws SQLException {
        ArrayList<RequestItem> list = new ArrayList<>();
        while (rs.next()) {
            list.add(readRequest(rs));
        }
        return list;
    }

    private static RequestItem getRequestForUpdate(Connection conn, int requestId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT request_id, uid, name, mobile, city, blood_group, quantity, status, is_emergency FROM blood_requests WHERE request_id=? FOR UPDATE")) {
            pstmt.setInt(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return readRequest(rs);
                }
            }
        }
        return null;
    }

    private static RequestItem readRequest(ResultSet rs) throws SQLException {
        RequestItem item = new RequestItem();
        item.requestId = rs.getInt("request_id");
        item.uid = rs.getString("uid");
        item.name = rs.getString("name");
        item.mobile = rs.getString("mobile");
        item.city = rs.getString("city");
        item.bloodGroup = rs.getString("blood_group");
        item.quantity = rs.getInt("quantity");
        item.status = rs.getString("status");
        item.emergency = rs.getBoolean("is_emergency");
        return item;
    }

    private static void updateRequestStatus(Connection conn, int requestId, String status, String timestampColumn) throws SQLException {
        String query = timestampColumn == null
                ? "UPDATE blood_requests SET status=? WHERE request_id=?"
                : "UPDATE blood_requests SET status=?, " + timestampColumn + "=CURRENT_TIMESTAMP WHERE request_id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
        }
    }

    private static void reduceInventory(Connection conn, String requestBloodGroup, int requiredQuantity) throws SQLException {
        int remaining = requiredQuantity;
        Map<Integer, Integer> deductions = new HashMap<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, blood_group, quantity FROM blood_inventory WHERE expiry_date >= CURDATE() AND quantity > 0 ORDER BY expiry_date ASC FOR UPDATE")) {
            while (rs.next() && remaining > 0) {
                if (!BloodCompatibility.canDonateTo(rs.getString("blood_group"), requestBloodGroup)) {
                    continue;
                }
                int id = rs.getInt("id");
                int available = rs.getInt("quantity");
                int used = Math.min(available, remaining);
                deductions.put(id, used);
                remaining -= used;
            }
        }

        if (remaining > 0) {
            throw new SQLException("Not enough compatible inventory");
        }
        applyDeductions(conn, deductions);
    }

    private static void reduceExactInventory(Connection conn, String bloodGroup, int requiredQuantity) throws SQLException {
        int remaining = requiredQuantity;
        Map<Integer, Integer> deductions = new HashMap<>();

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id, quantity FROM blood_inventory WHERE blood_group=? AND expiry_date >= CURDATE() AND quantity > 0 ORDER BY expiry_date ASC FOR UPDATE")) {
            pstmt.setString(1, bloodGroup);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next() && remaining > 0) {
                    int id = rs.getInt("id");
                    int available = rs.getInt("quantity");
                    int used = Math.min(available, remaining);
                    deductions.put(id, used);
                    remaining -= used;
                }
            }
        }

        if (remaining > 0) {
            throw new SQLException("Not enough inventory to remove");
        }
        applyDeductions(conn, deductions);
    }

    private static void applyDeductions(Connection conn, Map<Integer, Integer> deductions) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE blood_inventory SET quantity = quantity - ? WHERE id=? AND quantity >= ?")) {
            for (Map.Entry<Integer, Integer> deduction : deductions.entrySet()) {
                pstmt.setInt(1, deduction.getValue());
                pstmt.setInt(2, deduction.getKey());
                pstmt.setInt(3, deduction.getValue());
                if (pstmt.executeUpdate() == 0) {
                    throw new SQLException("Inventory changed while processing. Try again.");
                }
            }
        }
    }
}
