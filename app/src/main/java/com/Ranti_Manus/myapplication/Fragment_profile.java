package com.Ranti_Manus.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
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
 * Use the {@link Fragment_profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_profile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_profile() {
        // Required empty public constructor

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_profile newInstance(String param1, String param2) {
        Fragment_profile fragment = new Fragment_profile();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button edit_btn = view.findViewById(R.id.edit_btn);

        TextView name,gender,bloodGroup,email,mobile_num,city;
        name= view.findViewById(R.id.name);

        gender= view.findViewById(R.id.gender);
        bloodGroup = view.findViewById(R.id.blood_group);
        email = view.findViewById(R.id.email);
        mobile_num = view.findViewById(R.id.mobile_num);
        city = view.findViewById(R.id.city);

//        FireManager manager = new FireManager();
//        Model_profile profile = manager.getProfile(ctx);
//        Model_profile[] profile = manager.getUserProfile(ctx);
//        name.setText(profile[0].name);


//

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                name.setText(profile.name);
//            }
//        },10000);


        Context context = requireContext();
        android.content.SharedPreferences sharedPreferences = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", null);
        if (uid != null) {
            String query = "SELECT * FROM users WHERE uid = ?";
            DatabaseHelper.executeQuery(query, pstmt -> pstmt.setString(1, uid), rs -> {
                if (rs.next()) {
                    Model_profile p = new Model_profile();
                    p.name = rs.getString("name");
                    p.gender = rs.getString("gender");
                    p.blood_group = rs.getString("blood_group");
                    p.email = rs.getString("email");
                    p.mobile = rs.getString("mobile");
                    p.city = rs.getString("city");
                    return p;
                }
                return null;
            }, new DatabaseHelper.DbCallback<Model_profile>() {
                @Override
                public void onSuccess(Model_profile profile) {
                    if (!isAdded()) return;
                    if (profile == null) {
                        Toast.makeText(requireContext(), "please create profile first", Toast.LENGTH_SHORT).show();
                    } else {
                        name.setText(profile.name);
                        gender.setText(profile.gender);
                        bloodGroup.setText(profile.blood_group);
                        email.setText(profile.email);
                        mobile_num.setText(profile.mobile);
                        city.setText(profile.city);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),"failed to read data ",Toast.LENGTH_SHORT).show();
                }
            });
        }

        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(ctx, "Button clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
