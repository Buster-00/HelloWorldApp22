package org.pytorch.helloworld;

import static org.opencv.imgproc.Imgproc.bilateralFilter;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.photo.Photo.seamlessClone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;

import helper.GraphCutSeamFinderHelper;

/*
* this class process the final result of the image
* */

public class ResultActivity extends AppCompatActivity {

    public native void user_mask_seamlessclone(long im1_p_addr, long im2_p_addr, long des_addr, int x, int y, int width, int height);

    //widget
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //initialize the widgets
        imageView = findViewById(R.id.imageView);

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

        //retrieve imgRE1 and imgRE2 from application
        Data_app data_app = (Data_app) getApplication();
        HashMap<String, Mat> hashMap_mats = data_app.getHashMap_Mats();
        imgRE1 = hashMap_mats.get("imgRE1");
        imgRE2 = hashMap_mats.get("imgRE2");

        //Convert 8UC4 to 32FC3

        //Seamless clone
        //Mat result = user_mask_seamlessClone_java(imgRE1, imgRE2, x, y, width, height);

        //graph cut seamfinder
        Mat result = GraphCutSeamFinderHelper.GraphCutSeamFinder(imgRE1, imgRE2, x,y, (Math.max(width, height)));

        //Bilateral Filter
        Mat temp = new Mat();
        bilateralFilter(result,temp,0, 2, 5);

        Bitmap bm = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(temp, bm);

        imageView.setImageBitmap(bm);
    }

    private  Mat user_mask_seamlessClone_java(Mat imgRE1, Mat imgRE2, int x, int y, int width, int height)
    {
        Mat imgRE1_3C = new Mat();
        Mat imgRE2_3C = new Mat();
        cvtColor(imgRE1, imgRE1_3C, Imgproc.COLOR_BGRA2BGR);
        cvtColor(imgRE2, imgRE2_3C, Imgproc.COLOR_BGRA2BGR);

        Mat mask = new Mat(imgRE1_3C.rows(), imgRE1_3C.cols(), CvType.CV_8UC3, new Scalar(0,0,0));

        if (width / 2 != 0.5) width = width + 1;

        if (width / 2 != 0.5) height = height + 1;

        Point p3 = new Point(x ,y);
        Point p4 = new Point(x + width, y + height);
        Scalar colorRectangle2 = new Scalar(255,255,255);

        rectangle(mask, p3, p4, colorRectangle2, -1);

        Point center = new Point(x + (int)(width / 2), y + (int)(height/2));
        Mat result = new Mat(imgRE1_3C.rows(), imgRE1_3C.cols(), CvType.CV_8UC3);
        seamlessClone(imgRE2_3C, imgRE1_3C, mask, center, result, 1);

        return result;
    }


}