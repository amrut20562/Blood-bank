package com.Ranti_Manus.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class InventoryManagementActivity extends AppCompatActivity {
    Spinner bloodSpinner;
    EditText quantityInput, expiryInput;
    TextView helpText;
    String adminUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Inventory Management");

        SharedPreferences prefs = getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        adminUid = prefs.getString("uid", "");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        setContentView(root);

        helpText = new TextView(this);
        helpText.setText("Expiry format: YYYY-MM-DD");
        root.addView(helpText);

        bloodSpinner = new Spinner(this);
        bloodSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"A+","A-","B+","B-","O+","O-","AB+","AB-"}));
        root.addView(bloodSpinner);

        quantityInput = new EditText(this);
        quantityInput.setHint("Quantity");
        quantityInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        root.addView(quantityInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        expiryInput = new EditText(this);
        expiryInput.setHint("Expiry Date YYYY-MM-DD");
        root.addView(expiryInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button addBtn = new Button(this);
        addBtn.setText("Add Inventory");
        addBtn.setAllCaps(false);
        root.addView(addBtn);

        Button removeBtn = new Button(this);
        removeBtn.setText("Remove Inventory");
        removeBtn.setAllCaps(false);
        root.addView(removeBtn);

        addBtn.setOnClickListener(v -> addInventory());
        removeBtn.setOnClickListener(v -> removeInventory());
    }

    private void addInventory() {
        String bloodGroup = bloodSpinner.getSelectedItem().toString();
        int quantity = ValidationUtils.parsePositiveQuantity(quantityInput.getText().toString());
        String expiryDate = expiryInput.getText().toString().trim();
        if (quantity <= 0 || !expiryDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Enter valid quantity and expiry date", Toast.LENGTH_SHORT).show();
            return;
        }
        BloodBankService.addManualInventory(adminUid, bloodGroup, quantity, expiryDate, callback("Inventory added"));
    }

    private void removeInventory() {
        String bloodGroup = bloodSpinner.getSelectedItem().toString();
        int quantity = ValidationUtils.parsePositiveQuantity(quantityInput.getText().toString());
        if (quantity <= 0) {
            Toast.makeText(this, "Enter valid quantity", Toast.LENGTH_SHORT).show();
            return;
        }
        BloodBankService.removeManualInventory(adminUid, bloodGroup, quantity, callback("Inventory removed"));
    }

    private DatabaseHelper.DbCallback<Boolean> callback(String message) {
        return new DatabaseHelper.DbCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(InventoryManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                quantityInput.setText("");
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InventoryManagementActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }
}
