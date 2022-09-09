package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class SelectActivity extends AppCompatActivity {

    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        //request permission
        requestPermission();

        //initialize widgets
        FloatingActionButton btn_default = findViewById(R.id.btn_default);
        FloatingActionButton btn_camera = findViewById(R.id.btn_camera);
        FloatingActionButton btn_debug = findViewById(R.id.btn_debug);
        Button btn_crop = findViewById(R.id.btn_crop);

        //Bind onClick function
        btn_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        btn_debug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, debugActivity.class);
                startActivity(intent);
            }
        });

        btn_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, CropActivity.class);
                startActivity(intent);
            }
        });
    }

    private void requestPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, permissions,100);
        }
    }
}