package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class RequestManagementActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<BloodBankService.RequestItem> requests = new ArrayList<>();
    int selectedIndex = -1;
    String adminUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Manage Requests");

        SharedPreferences prefs = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        adminUid = prefs.getString("uid", "");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);
        setContentView(root);

        listView = new ListView(this);
        root.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(buttons);

        Button approveBtn = makeButton("Approve");
        Button rejectBtn = makeButton("Reject");
        Button completeBtn = makeButton("Complete");
        buttons.addView(approveBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        buttons.addView(rejectBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        buttons.addView(completeBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        listView.setOnItemClickListener((parent, view, position, id) -> selectedIndex = position);
        approveBtn.setOnClickListener(v -> updateSelected("approved"));
        rejectBtn.setOnClickListener(v -> updateSelected("rejected"));
        completeBtn.setOnClickListener(v -> updateSelected("completed"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private void loadRequests() {
        BloodBankService.loadRequests(new DatabaseHelper.DbCallback<ArrayList<BloodBankService.RequestItem>>() {
            @Override
            public void onSuccess(ArrayList<BloodBankService.RequestItem> result) {
                requests = result;
                ArrayList<String> rows = new ArrayList<>();
                for (BloodBankService.RequestItem item : result) {
                    rows.add(item.displayText());
                }
                if (rows.isEmpty()) {
                    rows.add("No requests found.");
                }
                listView.setAdapter(new ArrayAdapter<>(RequestManagementActivity.this, android.R.layout.simple_list_item_activated_1, rows));
                selectedIndex = -1;
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RequestManagementActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateSelected(String status) {
        if (selectedIndex < 0 || selectedIndex >= requests.size()) {
            Toast.makeText(this, "Select a request first", Toast.LENGTH_SHORT).show();
            return;
        }
        int requestId = requests.get(selectedIndex).requestId;
        BloodBankService.updateRequestStatus(adminUid, requestId, status, new DatabaseHelper.DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(RequestManagementActivity.this, "Request updated", Toast.LENGTH_SHORT).show();
                loadRequests();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RequestManagementActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
