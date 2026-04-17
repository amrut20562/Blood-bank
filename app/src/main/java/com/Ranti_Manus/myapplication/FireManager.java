package com.Ranti_Manus.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FireManager {

    Model_BloodData model;
    Model_profile profile;

    public interface OperationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void user_logout(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("uid");
        editor.remove("role");
        editor.apply();
        
        Intent intent = new Intent(context, Login.class);
        context.startActivity(intent);
    }
    
    public void read_data(){
        String query = "SELECT COALESCE(br.name, u.name) AS name, COALESCE(br.mobile, u.mobile) AS mobile, br.blood_group " +
                "FROM blood_requests br LEFT JOIN users u ON br.uid = u.uid";
        DatabaseHelper.executeQuery(query, null, new DatabaseHelper.ResultSetAction<Void>() {
            @Override
            public Void process(ResultSet rs) throws SQLException {
                while(rs.next()) {
                    Request_model obj = new Request_model(
                            rs.getString("name"),
                            rs.getString("mobile"),
                            rs.getString("blood_group")
                    );
                    // Do something with obj
                }
                return null;
            }
        }, new DatabaseHelper.DbCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public void sendBloodRequest(Context context, String bloodGroup, String Quantity){
        sendBloodRequest(context, bloodGroup, Quantity, false, null);
    }

    public void sendBloodRequest(Context context, String bloodGroup, String Quantity, OperationCallback callback){
        sendBloodRequest(context, bloodGroup, Quantity, false, callback);
    }

    public void sendBloodRequest(Context context, String bloodGroup, String Quantity, boolean isEmergency, OperationCallback callback){
        SharedPreferences sharedPreferences = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);
        
        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        int parsedQuantity = ValidationUtils.parsePositiveQuantity(Quantity);
        if (parsedQuantity <= 0) {
            Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onFailure(new IllegalArgumentException("Invalid quantity"));
            return;
        }

        DatabaseHelper.executeTransaction(conn -> {
            String name = "";
            String mobile = "";
            String city = "";
            try (PreparedStatement userStmt = conn.prepareStatement("SELECT name, mobile, city FROM users WHERE uid=?")) {
                userStmt.setString(1, uid);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        name = rs.getString("name");
                        mobile = rs.getString("mobile");
                        city = rs.getString("city");
                    }
                }
            }

            String query = "INSERT INTO blood_requests (uid, name, mobile, city, blood_group, quantity, status, is_emergency) VALUES (?, ?, ?, ?, ?, ?, 'pending', ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, uid);
                pstmt.setString(2, name);
                pstmt.setString(3, mobile);
                pstmt.setString(4, city);
                pstmt.setString(5, bloodGroup);
                pstmt.setInt(6, parsedQuantity);
                pstmt.setBoolean(7, isEmergency);
                pstmt.executeUpdate();
            }
            return city == null ? "" : city;
        }, new DatabaseHelper.DbCallback<String>() {
            @Override
            public void onSuccess(String city) {
                Toast.makeText(context, isEmergency ? "Emergency request sent. Compatible donors notified." : "Blood request sent", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, MatchingDonorsActivity.class);
                intent.putExtra("blood_group", bloodGroup);
                intent.putExtra("city", city);
                intent.putExtra("is_emergency", isEmergency);
                context.startActivity(intent);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void setProfile(Context context, Model_profile profile){
        setProfile(context, profile, null);
    }

    public void setProfile(Context context, Model_profile profile, OperationCallback callback){
        SharedPreferences sharedPreferences = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);

        if (uid == null) {
            if (callback != null) callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }
        
        String query = "UPDATE users SET name=?, email=?, mobile=?, gender=?, blood_group=?, city=?, district=? WHERE uid=?";
        DatabaseHelper.executeUpdate(query, new DatabaseHelper.PreparedStatementAction() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, profile.name);
                pstmt.setString(2, profile.email);
                pstmt.setString(3, profile.mobile);
                pstmt.setString(4, profile.gender);
                pstmt.setString(5, profile.blood_group);
                pstmt.setString(6, profile.city);
                pstmt.setString(7, profile.address);
                pstmt.setString(8, uid);
            }
        }, new DatabaseHelper.DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onSuccess();
                } else {
                    onFailure(new SQLException("No user profile row was updated"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Failed to save profile", Toast.LENGTH_SHORT).show();
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void setDonor(Context context, Model_Donor donor){
        setDonor(context, donor, null);
    }

    public void setDonor(Context context, Model_Donor donor, OperationCallback callback){
        SharedPreferences sharedPreferences = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);

        if (uid == null) {
            if (callback != null) callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }
        
        String query = "INSERT INTO donors (uid, name, blood_group, city, mobile) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=?, blood_group=?, city=?, mobile=?";
        DatabaseHelper.executeUpdate(query, new DatabaseHelper.PreparedStatementAction() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, uid);
                pstmt.setString(2, donor.name);
                pstmt.setString(3, donor.bloodGroup);
                pstmt.setString(4, donor.city);
                pstmt.setString(5, donor.mobile);
                pstmt.setString(6, donor.name);
                pstmt.setString(7, donor.bloodGroup);
                pstmt.setString(8, donor.city);
                pstmt.setString(9, donor.mobile);
            }
        }, new DatabaseHelper.DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Toast.makeText(context, "Donor details saved", Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onSuccess();
                } else {
                    onFailure(new SQLException("No donor row was inserted or updated"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Failed to save donor details", Toast.LENGTH_SHORT).show();
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void donateBlood(Context context, Model_Donor donor, int quantity, OperationCallback callback) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);

        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }
        if (quantity <= 0) {
            Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onFailure(new IllegalArgumentException("Invalid quantity"));
            return;
        }

        DatabaseHelper.executeTransaction(conn -> {
            int daysSinceDonation = 9999;
            boolean hasDonation = false;
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT last_donation_date, DATEDIFF(CURDATE(), last_donation_date) AS days_since FROM donors WHERE uid=? FOR UPDATE")) {
                pstmt.setString(1, uid);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        hasDonation = rs.getDate("last_donation_date") != null;
                        daysSinceDonation = rs.getInt("days_since");
                    }
                }
            }

            if (hasDonation && daysSinceDonation < 90) {
                throw new SQLException("Donation blocked. Donor must wait 90 days between donations.");
            }

            String donorQuery = "INSERT INTO donors (uid, name, blood_group, city, mobile, last_donation_date, is_available) " +
                    "VALUES (?, ?, ?, ?, ?, CURDATE(), TRUE) ON DUPLICATE KEY UPDATE name=?, blood_group=?, city=?, mobile=?, last_donation_date=CURDATE(), is_available=TRUE";
            try (PreparedStatement pstmt = conn.prepareStatement(donorQuery)) {
                pstmt.setString(1, uid);
                pstmt.setString(2, donor.name);
                pstmt.setString(3, donor.bloodGroup);
                pstmt.setString(4, donor.city);
                pstmt.setString(5, donor.mobile);
                pstmt.setString(6, donor.name);
                pstmt.setString(7, donor.bloodGroup);
                pstmt.setString(8, donor.city);
                pstmt.setString(9, donor.mobile);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO donations (donor_uid, blood_group, quantity, donation_date, expiry_date, status) VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 42 DAY), 'completed')")) {
                pstmt.setString(1, uid);
                pstmt.setString(2, donor.bloodGroup);
                pstmt.setInt(3, quantity);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO blood_inventory (blood_group, quantity, expiry_date, source) VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL 42 DAY), 'donation')")) {
                pstmt.setString(1, donor.bloodGroup);
                pstmt.setInt(2, quantity);
                pstmt.executeUpdate();
            }

            return true;
        }, new DatabaseHelper.DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(context, "Donation recorded and inventory updated", Toast.LENGTH_SHORT).show();
                if (callback != null) callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                if (callback != null) callback.onFailure(e);
            }
        });
    }
}
