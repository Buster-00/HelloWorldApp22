package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.Vector;

public class MatActivity extends AppCompatActivity {

    //widget
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat);

        //get mat vector
        mMat mats = (mMat) getIntent().getSerializableExtra("mats");
        Mat imgRE1 = mats.getMatVector().get(0);

        //transfer the mat to bitmap
        Bitmap bm = Bitmap.createBitmap(imgRE1.cols(), imgRE1.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgRE1, bm);

        imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bm);
    }
}