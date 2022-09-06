package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class PostProcessActivity extends AppCompatActivity {

    //widegt
    ImageView img_1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_process);

        //initialize widget
        img_1 = findViewById(R.id.img_1);


    }
}