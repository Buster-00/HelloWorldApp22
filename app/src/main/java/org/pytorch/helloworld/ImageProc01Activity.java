package org.pytorch.helloworld;

import static org.opencv.imgproc.Imgproc.convexHull;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.photo.Photo.fastNlMeansDenoising;

import static helper.convexHullHelper.detectHighlightArea;

import android.content.Intent;
import android.os.Bundle;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.HashMap;

import helper.ssimHelper;

public class ImageProc01Activity extends ImageProcBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void postProcess() {

        //perform SSIM process
        //FileInputStream in = new FileInputStream(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/"))
        ssimHelper.ssim(imgRE1_crop_, imgRE2_crop_);

        //detect high light area with OpenCV technique
        Rect rect = detectHighlightArea(imgRE2_crop_);

        //turn to another activity
        Intent intent = new Intent(ImageProc01Activity.this, DisplayResultActivity.class);

        //put rect x, y, w, h
        Bundle bundle = new Bundle();
        bundle.putInt("x", rect.x);
        bundle.putInt("y", rect.y);
        bundle.putInt("w", rect.width);
        bundle.putInt("h", rect.height);
        intent.putExtras(bundle);

        //put mat data into Data_app
        HashMap<String, Mat> hashMap_mat = new HashMap<>();
        hashMap_mat.put("imgRE1", imgRE1_crop_);
        hashMap_mat.put("imgRE2", imgRE2_crop_);
        Data_app data_app = (Data_app) getApplication();
        data_app.setHashMap_Mats(hashMap_mat);

        //start activity
        startActivity(intent);
        finish();
    }

}