package com.Ranti_Manus.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    Context context;
    ArrayList<Model_Donor> donorList;
    LayoutInflater inflater;
    public CustomAdapter(Context ctx, ArrayList<Model_Donor> donorList){
        this.context = ctx;
        this.donorList = donorList;
        inflater = LayoutInflater.from(ctx);
    }
    @Override
    public int getCount() {
        return donorList.size();
    }

    @Override
    public Object getItem(int position) {
        return donorList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_list_view, parent, false);
        }

        TextView name,blood_group,mobile_num,city;
        name = (TextView) convertView.findViewById(R.id.name);
        blood_group = (TextView) convertView.findViewById(R.id.blood_group);
        mobile_num = (TextView) convertView.findViewById(R.id.mobile_num);
        city = (TextView) convertView.findViewById(R.id.city);

        name.setText(donorList.get(position).name);
        blood_group.setText(donorList.get(position).bloodGroup);
        mobile_num.setText(donorList.get(position).mobile);
        city.setText(donorList.get(position).city);


        return convertView;
    }
}
