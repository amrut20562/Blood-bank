package com.Ranti_Manus.myapplication;

public class Model_BloodRequest {

    public String BloodGroup;
    public int Quantity;

    public String Uid;

    public Model_BloodRequest(){

    }
    public Model_BloodRequest(String bloodGroup,int quantity){
        this.BloodGroup = bloodGroup ;
        this.Quantity = quantity;
    }

}
