package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Vector;

public class MatActivity extends AppCompatActivity {

    //widget
    ImageView imageView_1;
    ImageView imageView_2;
    FloatingActionButton btn_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat);

        //Initialize widgets
        imageView_1 = findViewById(R.id.imageView_1);

        imageView_2 = findViewById(R.id.imageView_2);


        //get mat vector
        Data_app data_app = (Data_app) getApplication();
        HashMap<String, Mat> hashMap_mat = data_app.getHashMap_Mats();
        Mat imgRE1 = hashMap_mat.get("imgRE1");
        Mat imgRE2 = hashMap_mat.get("imgRE2");

        //transfer the mat to bitmap
        Bitmap bm_RE1 = Bitmap.createBitmap(imgRE1.cols(), imgRE1.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgRE1, bm_RE1);

        Bitmap bm_RE2 = Bitmap.createBitmap(imgRE2.cols(), imgRE2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgRE2, bm_RE2);

        //set image view
        imageView_1.setImageBitmap(bm_RE1);
        imageView_2.setImageBitmap(bm_RE2);
        btn_next = findViewById(R.id.btn_next);

        //btn_next
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MatActivity.this, CropActivity.class);
                startActivity(intent);
            }
        });
    }
}