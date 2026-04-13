package com.Ranti_Manus.myapplication;

public class Request_model {
    public String name;
    public String mobile;
    public String blood_group;

    Request_model(String name,String mobile,String blood_group){
        this.name = name;
        this.mobile = mobile;
        this.blood_group= blood_group;
    }
    Request_model(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBlood_group() {
        return blood_group;
    }

    public void setBlood_group(String blood_group) {
        this.blood_group = blood_group;
    }
}
