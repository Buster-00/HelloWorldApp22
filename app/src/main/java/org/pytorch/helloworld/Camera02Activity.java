package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Camera02Activity extends CameraActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cfg.setOnCheckClass(Test02Activity.class);

    }
}