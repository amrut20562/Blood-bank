package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    View dash_1,dash_2,dash_3;
    LinearLayout layout_3;
    CardView log_out_card;

    Button search_btn,blood_info_btn,user_pr_btn;
    SharedPreferences sharedPreferences;
    TextView totalUnitsText, pendingRequestsText, donorCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String currentUserUid = sharedPreferences.getString("uid", null);

        if(currentUserUid == null){
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        dash_1= findViewById(R.id.dash_1);
        dash_2= findViewById(R.id.dash_2);
        dash_3= findViewById(R.id.dash_3);

        log_out_card= findViewById(R.id.log_out_card);

        search_btn = findViewById(R.id.search_btn);
        blood_info_btn= findViewById(R.id.blood_info_btn);
        user_pr_btn = findViewById(R.id.user_pr_btn);
        totalUnitsText = findViewById(R.id.total_units);
        pendingRequestsText = findViewById(R.id.pending_requests);
        donorCountText = findViewById(R.id.donor_count);

        LoadFragment(new Fragment_searchDoner(), true);
        loadDashboardStats();

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibility_manager(1);
                LoadFragment(new Fragment_searchDoner(), false);

            }
        });
        blood_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibility_manager(2);
                LoadFragment(new FragmentBoodInfo(), false);
            }
        });
        user_pr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibility_manager(3);
                LoadFragment(new Fragment_profile(), false);
            }
        });

        log_out_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FireManager manager = new FireManager();
                manager.user_logout(MainActivity.this);
                finish();
            }
        });




    }

    @Override
    protected void onResume() {
        super.onResume();
        if (totalUnitsText != null) {
            loadDashboardStats();
        }
    }


    public void LoadFragment(Fragment fragment, boolean flag){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        if(flag){
            ft.add(R.id.data_display_layout, fragment);
        }
        else {
            ft.replace(R.id.data_display_layout,fragment);
        }

        ft.commit();
    }






    public void visibility_manager(int visibility_code){
        if(visibility_code==1){
            dash_1.setVisibility(View.VISIBLE);
            dash_2.setVisibility(View.INVISIBLE);
            dash_3.setVisibility(View.INVISIBLE);

        }
        else if (visibility_code==2) {
            dash_1.setVisibility(View.INVISIBLE);
            dash_2.setVisibility(View.VISIBLE);
            dash_3.setVisibility(View.INVISIBLE);
        }
        else if (visibility_code==3) {
            dash_1.setVisibility(View.INVISIBLE);
            dash_2.setVisibility(View.INVISIBLE);
            dash_3.setVisibility(View.VISIBLE);
        }


    }

    private void loadDashboardStats() {
        BloodBankService.loadDashboardStats(new DatabaseHelper.DbCallback<BloodBankService.DashboardStats>() {
            @Override
            public void onSuccess(BloodBankService.DashboardStats stats) {
                totalUnitsText.setText("Units: " + stats.totalUnits);
                pendingRequestsText.setText("Pending: " + stats.pendingRequests);
                donorCountText.setText("Donors: " + stats.donorCount);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }



}
