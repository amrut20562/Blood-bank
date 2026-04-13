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
        sendBloodRequest(context, bloodGroup, Quantity, null);
    }

    public void sendBloodRequest(Context context, String bloodGroup, String Quantity, OperationCallback callback){
        SharedPreferences sharedPreferences = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);
        
        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        int parsedQuantity;
        try {
            parsedQuantity = Integer.parseInt(Quantity.trim());
            if (parsedQuantity <= 0) {
                throw new NumberFormatException("Quantity must be greater than zero");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onFailure(e);
            return;
        }

        String query = "INSERT INTO blood_requests (uid, blood_group, quantity) VALUES (?, ?, ?)";
        DatabaseHelper.executeUpdate(query, new DatabaseHelper.PreparedStatementAction() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, uid);
                pstmt.setString(2, bloodGroup);
                pstmt.setInt(3, parsedQuantity);
            }
        }, new DatabaseHelper.DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Toast.makeText(context, "Blood request sent", Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onSuccess();
                } else {
                    onFailure(new SQLException("No blood request row was inserted"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show();
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
}
