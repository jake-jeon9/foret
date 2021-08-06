package com.example.foret_app_prototype.helper;

public class getIPAdress {

    private static getIPAdress instance = null;

    String ip = "http://13.125.27.1:8080";
    //String ip = "http://192.168.219.100:8081";

    public static getIPAdress getInstance() {
        if (instance == null) {
            instance = new getIPAdress();
        }
        return instance;
    }
    public getIPAdress(){};

    public String getIp() {
        return ip;
    }

}
