package org.pytorch.helloworld;

import static org.opencv.core.Core.findNonZero;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.convexHull;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.photo.Photo.fastNlMeansDenoising;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class Test01Activity extends TestBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void processFunc() {

        //warp and crop the images
        super.processFunc();

        //detect high light area with OpenCV technique
        Rect rect = detectHighlightArea(imgRE1_crop_2);
        rectangle(imgRE1_crop_2, rect, new Scalar(255,0,0,1), 5);

        //turn to another activity
        Intent intent = new Intent(Test01Activity.this, MatActivity.class);

        //put rect x, y, w, h
        intent.putExtra("x", rect.x);
        intent.putExtra("y", rect.y);
        intent.putExtra("w", rect.width);
        intent.putExtra("h", rect.height);

        //put mat data into Data_app
        HashMap<String, Mat> hashMap_mat = new HashMap<>();
        hashMap_mat.put("imgRE1", imgRE1_crop_1);
        hashMap_mat.put("imgRE2", imgRE1_crop_2);
        Data_app data_app = (Data_app) getApplication();
        data_app.setHashMap_Mats(hashMap_mat);

        //start activity
        startActivity(intent);
        finish();
    }


    //detect the high light area
    Rect detectHighlightArea(Mat src){

        Mat img = new Mat();
        src.copyTo(img);
        cvtColor(img, img, COLOR_BGR2GRAY);
        //bright threshold
        double bright_threshold = 200;

        //Denoising
        fastNlMeansDenoising(img, img);

        //Get a binary image out of a grayscale image
        Imgproc.threshold(img, img, bright_threshold, 255, Imgproc.THRESH_BINARY);

        MatOfPoint points = new MatOfPoint();
        findNonZero(img, points);

        MatOfInt hull = new MatOfInt();
        MatOfPoint hullPointMat = new MatOfPoint();
        ArrayList<Point> hullPointList = new ArrayList<>();

        //perform convexHull
        convexHull(points, hull, true);

        //add points to hullPointList
        for(int i = 0; i < hull.toList().size(); i++){
            hullPointList.add(points.toList().get(hull.toList().get(i)));
        }

        hullPointMat.fromList(hullPointList);

        //get rectangle
        Rect rect = boundingRect(hullPointMat);

        Log.e("rect", rect.toString());

        return rect;
    }
}