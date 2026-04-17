package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ActivityRequestBlood extends AppCompatActivity {
    private String bloodGroup = "A+";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_blood);
        
        Spinner Spinner_blood_group = findViewById(R.id.Spinner_blood_group);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ActivityRequestBlood.this, android.R.layout.simple_spinner_dropdown_item,new String[]{"A+","A-","B+","B-","O+","O-","AB+","AB-"});
        Spinner_blood_group.setAdapter(adapter);

        Button send_request_btn = findViewById(R.id.send_request_btn);
        EditText quantity = findViewById(R.id.quantity);
        CheckBox emergencyCheckbox = findViewById(R.id.emergency_checkbox);


        Spinner_blood_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bloodGroup = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bloodGroup = "A+";
            }
        });

        send_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code to send request

                String str_quantity = quantity.getText().toString().trim();
                int parsedQuantity = ValidationUtils.parsePositiveQuantity(str_quantity);
                if (parsedQuantity <= 0){
                    Toast.makeText(ActivityRequestBlood.this, "Enter a valid quantity", Toast.LENGTH_SHORT).show();
                }
                else{
                    send_request_btn.setEnabled(false);
                    FireManager manager = new FireManager();
                    Toast.makeText(ActivityRequestBlood.this, "sending request....", Toast.LENGTH_SHORT).show();
                    manager.sendBloodRequest(ActivityRequestBlood.this, bloodGroup, str_quantity, emergencyCheckbox.isChecked(), new FireManager.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            send_request_btn.setEnabled(true);
                        }
                    });
                }

            }
        });
        
    }
}
