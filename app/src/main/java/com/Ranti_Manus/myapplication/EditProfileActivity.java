package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class EditProfileActivity extends AppCompatActivity {

    RadioGroup radioGroup;
    String gender = "", bloodGroup = "A+";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Spinner Spinner_blood_group = findViewById(R.id.Spinner_blood_group);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfileActivity.this, android.R.layout.simple_spinner_dropdown_item,new String[]{"A+","A-","B+","B-","O+","O-","AB+","AB-"});
        Spinner_blood_group.setAdapter(adapter);


        EditText name,email,mobile,address,city;
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        mobile = findViewById(R.id.mobile_num);
        address = findViewById(R.id.address);
        city= findViewById(R.id.city);



        radioGroup = findViewById(R.id.gender_radio);
        Button save_btn= findViewById(R.id.edit_btn);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                Toast.makeText(EditProfileActivity.this, "calling method", Toast.LENGTH_SHORT).show();
                setGender(checkedId);
            }

        });



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

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str_name = name.getText().toString().trim();
                String str_email = email.getText().toString().trim();
                String str_mobile = mobile.getText().toString().trim();
                String str_address = address.getText().toString().trim();
                String str_city = city.getText().toString().trim();

                if(str_name.isEmpty() || str_email.isEmpty() || str_mobile.isEmpty() ||str_address.isEmpty() || str_city.isEmpty()||gender.isEmpty()){
                    Toast.makeText(EditProfileActivity.this, "please fill all fields", Toast.LENGTH_SHORT).show();
                }
                else {
                    Model_profile obj = new Model_profile(str_name,str_email,str_mobile,gender,bloodGroup,str_address,str_city);
                    FireManager manager = new FireManager();
                    save_btn.setEnabled(false);
                    manager.setProfile(EditProfileActivity.this, obj, new FireManager.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            save_btn.setEnabled(true);
                        }
                    });
                }
            }
        });



    }


    public void setGender(int checkedId){
        if(checkedId == R.id.male){
            gender = "male";
        }
        else if (checkedId == R.id.female){
            gender = "female";
        }
        else if (checkedId == R.id.other) {
            gender = "other";
        }
        else {
            gender = "";
        }

    }
}
