package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class RegisterDonorActivity extends AppCompatActivity {

    String bloodGroup = "A+";
    EditText name,mobile_num,city, donationQuantity;
    Button register_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registor_donor);
        Spinner Spinner_blood_group = findViewById(R.id.Spinner_blood_group);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterDonorActivity.this, android.R.layout.simple_spinner_dropdown_item,new String[]{"A+","A-","B+","B-","O+","O-","AB+","AB-"});
        Spinner_blood_group.setAdapter(adapter);

        name = findViewById(R.id.name);
        mobile_num = findViewById(R.id.mobile_num);
        city = findViewById(R.id.city);
        donationQuantity = findViewById(R.id.donation_quantity);

        register_btn = findViewById(R.id.register_btn);

        Spinner_blood_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bloodGroup = parent.getItemAtPosition(position).toString();
//                Toast.makeText(EditProfileActivity.this, "Selected: " + selectedText, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected (if needed)
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_name = name.getText().toString().trim();
                String str_mobile_num = mobile_num.getText().toString().trim();
                String str_city = city.getText().toString().trim();
                int parsedQuantity = ValidationUtils.parsePositiveQuantity(donationQuantity.getText().toString().trim());

                if(str_name.isEmpty()|| str_mobile_num.isEmpty() || str_city.isEmpty()){
                    Toast.makeText(RegisterDonorActivity.this, "please fill all fields ", Toast.LENGTH_SHORT).show();
                } else if (!ValidationUtils.isValidMobile(str_mobile_num)) {
                    Toast.makeText(RegisterDonorActivity.this, "Enter a valid 10 digit mobile number", Toast.LENGTH_SHORT).show();
                } else if (parsedQuantity <= 0) {
                    Toast.makeText(RegisterDonorActivity.this, "Enter a valid donation quantity", Toast.LENGTH_SHORT).show();
                }
                else {
                    android.content.SharedPreferences sharedPreferences = getSharedPreferences("BloodBankPrefs", android.content.Context.MODE_PRIVATE);
                    String uid = sharedPreferences.getString("uid", null);
                    if (uid == null) {
                        Toast.makeText(RegisterDonorActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FireManager manager = new FireManager();
                    Model_Donor donor = new Model_Donor(uid, str_name,bloodGroup,str_city,str_mobile_num);
                    register_btn.setEnabled(false);
                    Toast.makeText(RegisterDonorActivity.this, "Recording donation.....", Toast.LENGTH_SHORT).show();
                    manager.donateBlood(RegisterDonorActivity.this, donor, parsedQuantity, new FireManager.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            register_btn.setEnabled(true);
                        }
                    });
                }
            }
        });


    }
}
