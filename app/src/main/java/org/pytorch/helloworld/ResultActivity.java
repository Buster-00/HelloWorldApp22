package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {

    public native void user_mask_seamlessclone(long im1_p_addr, long im2_p_addr, long des_addr, int x, int y, int width, int height);

    //widget
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //initialize the widgets
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.tv_coordinate);

        //process image
        processImage();
    }

    private void processImage()
    {
        int x = getIntent().getIntExtra("x", 0);
        int y = getIntent().getIntExtra("y", 0);
        int width = getIntent().getIntExtra("width", 0);
        int height = getIntent().getIntExtra("height", 0);
        Mat imgRE1, imgRE2;
        Mat des = new Mat();

        //retrieve imgRE1 and imgRE2 from application
        Data_app data_app = (Data_app) getApplication();
        HashMap<String, Mat> hashMap_mats = data_app.getHashMap_Mats();
        imgRE1 = hashMap_mats.get("imgRE1");
        imgRE2 = hashMap_mats.get("imgRE2");

        //seamless clone
        user_mask_seamlessclone(imgRE1.getNativeObjAddr(), imgRE2.getNativeObjAddr(), des.getNativeObjAddr(),
                x, y, width, height);
        textView.setText(x + ", " + y + ", " + width + ", " + height + "");

        Bitmap bm = Bitmap.createBitmap(des.cols(), des.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(des, bm);

        imageView.setImageBitmap(bm);
    }
}