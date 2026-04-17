package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends AppCompatActivity {

    EditText email_in,pass_in;
    Button btn_login;

    ProgressBar progressBar;
    TextView Registration_intent;
    SharedPreferences sharedPreferences;

    private static class LoginResult {
        final String uid;
        final String role;
        final boolean needsPasswordMigration;

        LoginResult(String uid, String role, boolean needsPasswordMigration) {
            this.uid = uid;
            this.role = role;
            this.needsPasswordMigration = needsPasswordMigration;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        sharedPreferences = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("uid", null);
        String role = sharedPreferences.getString("role", "user");
        if(currentUser != null){
            Intent intent = "admin".equals(role)
                    ? new Intent(Login.this, AdminDashboardActivity.class)
                    : new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email_in= findViewById(R.id.email_in);
        pass_in= findViewById(R.id.pass_in);
        btn_login = findViewById(R.id.btn_login);
        Registration_intent = findViewById(R.id.register_intent);
        progressBar = findViewById(R.id.pr_bar);

        sharedPreferences = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);


        Registration_intent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,Resistration.class);
                startActivity(intent);
                finish();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = email_in.getText().toString().trim();
                String password = pass_in.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                String query = "SELECT uid, password, role FROM users WHERE email = ?";
                
                DatabaseHelper.executeQuery(query, new DatabaseHelper.PreparedStatementAction() {
                    @Override
                    public void setParameters(PreparedStatement pstmt) throws SQLException {
                        pstmt.setString(1, email);
                    }
                }, new DatabaseHelper.ResultSetAction<LoginResult>() {
                    @Override
                    public LoginResult process(ResultSet rs) throws SQLException {
                        if (rs.next()) {
                            String uid = rs.getString("uid");
                            String role = rs.getString("role");
                            String storedPassword = rs.getString("password");
                            if (PasswordHasher.matches(password, storedPassword)) {
                                return new LoginResult(uid, role, false);
                            }
                            if (password.equals(storedPassword)) {
                                return new LoginResult(uid, role, true);
                            }
                        }
                        return null;
                    }
                }, new DatabaseHelper.DbCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult result) {
                        progressBar.setVisibility(View.GONE);
                        if (result != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("uid", result.uid);
                            editor.putString("role", result.role);
                            editor.apply();

                            if (result.needsPasswordMigration) {
                                updateStoredPassword(result.uid, PasswordHasher.hash(password));
                            }
                            
                            Toast.makeText(Login.this, "login successful", Toast.LENGTH_SHORT).show();
                            onStart(); // Trigger navigation
                        } else {
                            Toast.makeText(Login.this, "Authentication failed. Invalid email or password.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    private void updateStoredPassword(String uid, String passwordHash) {
        String updateQuery = "UPDATE users SET password = ? WHERE uid = ?";
        DatabaseHelper.executeUpdate(updateQuery, new DatabaseHelper.PreparedStatementAction() {
            @Override
            public void setParameters(PreparedStatement pstmt) throws SQLException {
                pstmt.setString(1, passwordHash);
                pstmt.setString(2, uid);
            }
        }, new DatabaseHelper.DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }
}
