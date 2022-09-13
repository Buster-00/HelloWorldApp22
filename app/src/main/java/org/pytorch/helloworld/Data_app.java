package org.pytorch.helloworld;

import android.app.Application;

import org.opencv.core.Mat;

import java.util.HashMap;

public class Data_app extends Application {

    private HashMap<String, Mat> hashMap_Mats;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public HashMap<String, Mat> getHashMap_Mats() {
        return hashMap_Mats;
    }

    public void setHashMap_Mats(HashMap<String, Mat> hashMap_Mats) {
        this.hashMap_Mats = hashMap_Mats;
    }
}
