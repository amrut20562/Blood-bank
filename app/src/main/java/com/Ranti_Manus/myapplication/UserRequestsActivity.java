package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class UserRequestsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        setContentView(listView);
        setTitle("My Request Status");

        SharedPreferences prefs = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        if (uid == null) {
            finish();
            return;
        }

        BloodBankService.loadRequestsForUser(uid, new DatabaseHelper.DbCallback<ArrayList<BloodBankService.RequestItem>>() {
            @Override
            public void onSuccess(ArrayList<BloodBankService.RequestItem> requests) {
                ArrayList<String> rows = new ArrayList<>();
                for (BloodBankService.RequestItem request : requests) {
                    rows.add(request.displayText());
                }
                if (rows.isEmpty()) {
                    rows.add("No requests yet.");
                }
                listView.setAdapter(new ArrayAdapter<>(UserRequestsActivity.this, android.R.layout.simple_list_item_1, rows));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserRequestsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
