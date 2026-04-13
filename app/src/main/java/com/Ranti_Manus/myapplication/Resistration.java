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
import java.sql.SQLException;
import java.util.UUID;

public class Resistration extends AppCompatActivity {

    EditText email_in,pass_in,pass_in2;
    Button btn_register;

    ProgressBar progressBar;
    TextView login_intent;
    SharedPreferences sharedPreferences;

    private void redirectToMain() {
        Intent intent = new Intent(Resistration.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        sharedPreferences = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("uid", null);
        if(currentUser != null){
            redirectToMain();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resistration);

        email_in = findViewById(R.id.email_in);
        pass_in = findViewById(R.id.pass_in);
        btn_register = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.pr_bar);
        login_intent= findViewById(R.id.log_in_intent);
        pass_in2= findViewById(R.id.pass_in2);

        sharedPreferences = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);

        login_intent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Resistration.this,Login.class);
                startActivity(intent);
                finish();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                String email = email_in.getText().toString().trim();
                String password = pass_in.getText().toString().trim();
                String password2 = pass_in2.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Resistration.this, "enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password) || TextUtils.isEmpty(password2)){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Resistration.this, "enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!password.equals(password2)){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Resistration.this, "password does not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = UUID.randomUUID().toString();
                String passwordHash = PasswordHasher.hash(password);
                String query = "INSERT INTO users (uid, name, email, password) VALUES (?, ?, ?, ?)";
                
                DatabaseHelper.executeUpdate(query, new DatabaseHelper.PreparedStatementAction() {
                    @Override
                    public void setParameters(PreparedStatement pstmt) throws SQLException {
                        pstmt.setString(1, uid);
                        pstmt.setString(2, "User"); // Default name
                        pstmt.setString(3, email);
                        pstmt.setString(4, passwordHash);
                    }
                }, new DatabaseHelper.DbCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        progressBar.setVisibility(View.GONE);
                        if (result) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("uid", uid);
                            editor.apply();
                            
                            Toast.makeText(Resistration.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            redirectToMain();
                        } else {
                            Toast.makeText(Resistration.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Resistration.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}
