package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AdminDashboardActivity extends AppCompatActivity {
    TextView statsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Admin Dashboard");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        setContentView(root);

        statsText = new TextView(this);
        statsText.setTextSize(18);
        statsText.setText("Loading dashboard...");
        root.addView(statsText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button requestsBtn = makeButton("Manage Requests");
        Button donationsBtn = makeButton("Donation History");
        Button inventoryBtn = makeButton("Add / Remove Inventory");
        Button logoutBtn = makeButton("Logout");

        root.addView(requestsBtn);
        root.addView(donationsBtn);
        root.addView(inventoryBtn);
        root.addView(logoutBtn);

        requestsBtn.setOnClickListener(v -> startActivity(new Intent(this, RequestManagementActivity.class)));
        donationsBtn.setOnClickListener(v -> startActivity(new Intent(this, DonationHistoryActivity.class)));
        inventoryBtn.setOnClickListener(v -> startActivity(new Intent(this, InventoryManagementActivity.class)));
        logoutBtn.setOnClickListener(v -> new FireManager().user_logout(this));

        SharedPreferences prefs = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        if (!"admin".equals(prefs.getString("role", "user"))) {
            Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private void loadStats() {
        BloodBankService.loadDashboardStats(new DatabaseHelper.DbCallback<BloodBankService.DashboardStats>() {
            @Override
            public void onSuccess(BloodBankService.DashboardStats stats) {
                statsText.setText("Total units: " + stats.totalUnits +
                        "\nPending requests: " + stats.pendingRequests +
                        "\nAvailable donors: " + stats.donorCount);
            }

            @Override
            public void onFailure(Exception e) {
                statsText.setText("Failed to load dashboard");
            }
        });
    }
}
