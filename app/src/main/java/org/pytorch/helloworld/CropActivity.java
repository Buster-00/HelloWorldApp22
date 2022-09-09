package org.pytorch.helloworld;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.edmodo.cropper.CropImageView;

import org.pytorch.helloworld.R;

import java.io.IOException;

import crop.OnCropListener;
import crop.mCropImagheView;

public class CropActivity extends AppCompatActivity {

    //widget
    CropImageView cropImageView;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        //Initialize widget
        cropImageView = findViewById(R.id.crop_view);
        cropImageView.setAspectRatio(5, 10);
        cropImageView.setFixedAspectRatio(false);
        cropImageView.setGuidelines(1);
        try {
            cropImageView.setImageBitmap(BitmapFactory.decodeStream(getAssets().open("TestSample_4.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        cropImageView.setOnCapturedPointerListener(new View.OnCapturedPointerListener() {
            @Override
            public boolean onCapturedPointer(View view, MotionEvent event) {
                return false;
            }
        });

        cropImageView.setOnCropListener(new OnCropListener() {
            @Override
            public void onCrop(int cropX, int cropY, int cropWidth, int cropHeight) {
                Log.e("cord", cropX + " " + cropY + " " + cropWidth + " " + cropHeight);
            }
        });

        //get cropped image
        Button btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = cropImageView.getCroppedImage();
                ImageView imageView = findViewById(R.id.img_view);
                imageView.setImageBitmap(croppedImage);
            }
        });

    }
}