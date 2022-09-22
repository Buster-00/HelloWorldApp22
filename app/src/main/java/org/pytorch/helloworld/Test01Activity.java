package org.pytorch.helloworld;

import android.content.Intent;
import android.os.Bundle;

import org.opencv.core.Mat;

import java.util.HashMap;

public class Test01Activity extends TestBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void processFunc() {
        super.processFunc();

        //turn to another activity
        Intent intent = new Intent(Test01Activity.this, MatActivity.class);
        HashMap<String, Mat> hashMap_mat = new HashMap<>();
        hashMap_mat.put("imgRE1", imgRE1_crop_1);
        hashMap_mat.put("imgRE2", imgRE1_crop_2);
        Data_app data_app = (Data_app) getApplication();
        data_app.setHashMap_Mats(hashMap_mat);
        startActivity(intent);
        finish();
    }
}