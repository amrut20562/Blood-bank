package com.Ranti_Manus.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentBoodInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentBoodInfo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button blood_request_btn;
    Button Donate_btn;

    public FragmentBoodInfo() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentBoodInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentBoodInfo newInstance(String param1, String param2) {
        FragmentBoodInfo fragment = new FragmentBoodInfo();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_bood_info, container, false);
        blood_request_btn = view.findViewById(R.id.blood_request_btn);
        Donate_btn = view.findViewById(R.id.Donate_btn);
        TextView A_plus ,A_minus,B_plus,B_minus,O_plus,O_minus,AB_plus,AB_minus;

        A_plus = view.findViewById(R.id.A_plus);
        A_minus = view.findViewById(R.id.A_minus);
        B_plus = view.findViewById(R.id.B_plus);
        B_minus = view.findViewById(R.id.B_minus);
        O_plus = view.findViewById(R.id.O_plus);
        O_minus = view.findViewById(R.id.O_minus);
        AB_plus = view.findViewById(R.id.AB_plus);
        AB_minus = view.findViewById(R.id.AB_minus);

        String query = "SELECT blood_group, quantity FROM blood_data";
        DatabaseHelper.executeQuery(query, null, rs -> {
            Model_BloodData data = new Model_BloodData();
            while (rs.next()) {
                String quantity = String.valueOf(rs.getInt("quantity"));
                String group = rs.getString("blood_group");
                if ("A+".equals(group)) {
                    data.A_plus = quantity;
                } else if ("A-".equals(group)) {
                    data.A_minus = quantity;
                } else if ("B+".equals(group)) {
                    data.B_plus = quantity;
                } else if ("B-".equals(group)) {
                    data.B_minus = quantity;
                } else if ("O+".equals(group)) {
                    data.O_plus = quantity;
                } else if ("O-".equals(group)) {
                    data.O_minus = quantity;
                } else if ("AB+".equals(group)) {
                    data.AB_plus = quantity;
                } else if ("AB-".equals(group)) {
                    data.AB_minus = quantity;
                }
            }
            return data;
        }, new DatabaseHelper.DbCallback<Model_BloodData>() {
            @Override
            public void onSuccess(Model_BloodData modelBloodData) {
                if (!isAdded()) return;
                if(modelBloodData != null) {
                    A_plus.setText(modelBloodData.A_plus);
                    A_minus.setText(modelBloodData.A_minus);
                    B_plus.setText(modelBloodData.B_plus);
                    B_minus.setText(modelBloodData.B_minus);
                    O_plus.setText(modelBloodData.O_plus);
                    O_minus.setText(modelBloodData.O_minus);
                    AB_plus.setText(modelBloodData.AB_plus);
                    AB_minus.setText(modelBloodData.AB_minus);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to read data ", Toast.LENGTH_SHORT).show();
            }
        });

        blood_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ActivityRequestBlood.class);
                startActivity(intent);
            }
        });
        Donate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), RegisterDonorActivity.class);
                try {
                    startActivity(intent);
                }
                catch (Exception e){
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return  view;

    }
}
