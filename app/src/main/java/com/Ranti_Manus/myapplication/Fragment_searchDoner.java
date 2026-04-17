package com.Ranti_Manus.myapplication;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_searchDoner#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_searchDoner extends Fragment {

    ArrayList<Model_Donor> dataList = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_searchDoner() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_searchDoner.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_searchDoner newInstance(String param1, String param2) {
        Fragment_searchDoner fragment = new Fragment_searchDoner();
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

//        Model_Donor modelDonor = new Model_Donor(" udi ","shubham","A+","satara","1234567890");
//
//        dataList.add(modelDonor);
//        dataList.add(modelDonor);
//        dataList.add(modelDonor);
//        dataList.add(modelDonor);
//        dataList.add(modelDonor);

        View view = inflater.inflate(R.layout.fragment_search_doner, container, false);
//        TextView textView = view.findViewById(R.id.Donor_text);
        ListView listView = view.findViewById(R.id.search_list);
//        data_provider provider = new data_provider();

//        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, provider.getDonorList());
//        listView.setAdapter(itemsAdapter);
        Context context = requireContext();
        CustomAdapter adapter = new CustomAdapter(context, dataList);
        listView.setAdapter(adapter);



        android.content.SharedPreferences prefs = context.getSharedPreferences("BloodBankPrefs", Context.MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        String query = "SELECT d.uid, d.name, d.blood_group, d.city, d.mobile, (SELECT blood_group FROM users WHERE uid=?) AS user_blood_group FROM donors d WHERE d.is_available=TRUE";
        DatabaseHelper.executeQuery(query, pstmt -> pstmt.setString(1, uid), rs -> {
            java.util.ArrayList<Model_Donor> list = new java.util.ArrayList<>();
            while (rs.next()) {
                String userBloodGroup = rs.getString("user_blood_group");
                String donorGroup = rs.getString("blood_group");
                if (userBloodGroup == null || userBloodGroup.trim().isEmpty() || BloodCompatibility.canDonateTo(donorGroup, userBloodGroup)) {
                    Model_Donor d = new Model_Donor(
                            rs.getString("uid"),
                            rs.getString("name"),
                            donorGroup,
                            rs.getString("city"),
                            rs.getString("mobile")
                    );
                    list.add(d);
                }
            }
            return list;
        }, new DatabaseHelper.DbCallback<java.util.ArrayList<Model_Donor>>() {
            @Override
            public void onSuccess(java.util.ArrayList<Model_Donor> result) {
                if (!isAdded()) return;
                dataList.clear();
                if(result != null) {
                    dataList.addAll(result);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to read donors: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        return view;
    }
}
