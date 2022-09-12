package org.pytorch.helloworld;

import static camera.mCameraFragment.PICTURE_1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;

import org.pytorch.helloworld.R;

import java.io.IOException;

import camera.CameraParam;
import crop.OnCropListener;
import crop.mCropImagheView;

public class CropActivity extends AppCompatActivity {

    //widget
    CropImageView cropImageView;
    TextView tv_cordinate;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        //Initialize widget
        cropImageView = findViewById(R.id.crop_view);
        tv_cordinate = findViewById(R.id.tv_cordinate);

        cropImageView.setAspectRatio(5, 10);
        cropImageView.setFixedAspectRatio(false);
        cropImageView.setGuidelines(1);

        //get image uri
        Uri uri_1 = Uri.parse(getIntent().getExtras().getString(PICTURE_1));

        //set image
        try {
            Bitmap bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri_1));
            //bm = CameraParam.rotateBitmapByDegree(bm, 90);
            cropImageView.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cropImageView.setOnCropListener(new OnCropListener() {
            @Override
            public void onCrop(int cropX, int cropY, int cropWidth, int cropHeight) {
                Log.e("cord", cropX + " " + cropY + " " + cropWidth + " " + cropHeight);
                tv_cordinate.setText("(" + cropX + ", " + cropY + ", " + cropWidth + ", " + cropHeight + ")");
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