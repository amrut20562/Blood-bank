package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MatchingDonorsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        TextView emptyView = new TextView(this);
        emptyView.setText("No compatible donors found in your city.");
        listView.setEmptyView(emptyView);
        setContentView(listView);
        addContentView(emptyView, new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));

        String bloodGroup = getIntent().getStringExtra("blood_group");
        String city = getIntent().getStringExtra("city");
        boolean emergency = getIntent().getBooleanExtra("is_emergency", false);
        setTitle(emergency ? "Emergency Donor Matches" : "Compatible Donors");

        BloodBankService.findCompatibleDonors(bloodGroup, city == null ? "" : city, new DatabaseHelper.DbCallback<ArrayList<Model_Donor>>() {
            @Override
            public void onSuccess(ArrayList<Model_Donor> donors) {
                ArrayList<String> rows = new ArrayList<>();
                for (Model_Donor donor : donors) {
                    rows.add(donor.name + " | " + donor.bloodGroup + " | " + donor.mobile + " | " + donor.city);
                }
                if (emergency && !rows.isEmpty()) {
                    Toast.makeText(MatchingDonorsActivity.this, "UI simulation: compatible donors notified", Toast.LENGTH_LONG).show();
                }
                listView.setAdapter(new ArrayAdapter<>(MatchingDonorsActivity.this, android.R.layout.simple_list_item_1, rows));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MatchingDonorsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
