package com.Ranti_Manus.myapplication;

public class Model_profile {
    public String name,email = " ",mobile=" ",gender=" ",blood_group=" ",address=" ",city=" ";

    public Model_profile(){

    }
    public Model_profile(String name,String email,String mobile,String gender,String blood_group,String address,String city){

        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.gender = gender;
        this.blood_group = blood_group;
        this.address = address;
        this.city= city;
    }

}
