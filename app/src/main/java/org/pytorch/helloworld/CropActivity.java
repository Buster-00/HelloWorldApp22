package org.pytorch.helloworld;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.edmodo.cropper.CropImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import crop.OnCropListener;

public class CropActivity extends AppCompatActivity {

    //coordinates
    String coordinates;
    int x, y, width, height;
    boolean isUsingGraphCut = false;
    boolean isUsingGradientMask = false;

    //widget
    CropImageView cropImageView;
    Switch sw_graphCut;
    Switch sw_gradientMask;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white_translucent));//设置状态栏颜色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //Initialize widget
        cropImageView = findViewById(R.id.crop_view);
        sw_graphCut = findViewById(R.id.switch1);
        sw_gradientMask = findViewById(R.id.switch2);
        RelativeLayout relativeLayout = findViewById(R.id.Layout_relative);
        Log.e("width", ""+relativeLayout.getWidth());

        sw_graphCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isUsingGraphCut = !isUsingGraphCut;
            }
        });

        sw_gradientMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUsingGradientMask = !isUsingGradientMask;
            }
        });

        //get rect x, y, w, h
        x = getIntent().getIntExtra("x",0);
        y = getIntent().getIntExtra("y",0);
        width = getIntent().getIntExtra("w",0);
        height = getIntent().getIntExtra("h",0);

        cropImageView.setCoordinates(x, y, width, height);
        cropImageView.setAspectRatio(5, 10);
        cropImageView.setFixedAspectRatio(false);
        cropImageView.setGuidelines(1);


        //cropImageView.mPressedHandle.updateCropWindow(10,10,cropImageView.mBitmapRect, cropImageView.mSnapRadius);
        //get image uri
        Data_app data_app = (Data_app) getApplication();
        HashMap<String, Mat> hashMap_mats = data_app.getHashMap_Mats();
        Mat imgRE1 = hashMap_mats.get("imgRE1");

        //set image (imgRE1)
        Bitmap bm = Bitmap.createBitmap(imgRE1.cols(), imgRE1.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgRE1, bm);
        bm = Bitmap.createScaledBitmap(bm,768,1024, true);
        cropImageView.setImageBitmap(bm);

        //execute when cropImageView.getCroppedImage()
        cropImageView.setOnCropListener(new OnCropListener() {
            @Override
            public void onCrop(int cropX, int cropY, int cropWidth, int cropHeight) {
                x = cropX;
                y = cropY;
                width = cropWidth;
                height = cropHeight;
            }
        });

        //get cropped image
        Button btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = cropImageView.getCroppedImage();
                Intent intent = new Intent(CropActivity.this, FinalResultActivity.class);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                byte[] bitmapByte = out.toByteArray();
                intent.putExtra("bp", bitmapByte);
                intent.putExtra("coordinates", coordinates);
                intent.putExtra("x", x);
                intent.putExtra("y", y);
                intent.putExtra("width", width);
                intent.putExtra("height", height);
                intent.putExtra("isUsingGraphCut", isUsingGraphCut);
                intent.putExtra("isUsingGradientMask", isUsingGradientMask);
                startActivity(intent);
            }
        });

    }
}