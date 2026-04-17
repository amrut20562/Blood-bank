package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DonationHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Donation History");
        ListView listView = new ListView(this);
        setContentView(listView);

        BloodBankService.loadDonationHistory(new DatabaseHelper.DbCallback<ArrayList<BloodBankService.DonationItem>>() {
            @Override
            public void onSuccess(ArrayList<BloodBankService.DonationItem> donations) {
                ArrayList<String> rows = new ArrayList<>();
                for (BloodBankService.DonationItem donation : donations) {
                    rows.add(donation.displayText());
                }
                if (rows.isEmpty()) {
                    rows.add("No donations found.");
                }
                listView.setAdapter(new ArrayAdapter<>(DonationHistoryActivity.this, android.R.layout.simple_list_item_1, rows));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(DonationHistoryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
