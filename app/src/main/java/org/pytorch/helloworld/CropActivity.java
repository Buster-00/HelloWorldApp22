package org.pytorch.helloworld;

import static camera.mCameraFragment.PICTURE_1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.pytorch.helloworld.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.Result;

import camera.CameraParam;
import crop.OnCropListener;
import crop.mCropImagheView;

public class CropActivity extends AppCompatActivity {

    //coordinates
    String coordinates;
    int x, y, width, height;

    //widget
    CropImageView cropImageView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        //Initialize widget
        cropImageView = findViewById(R.id.crop_view);
        RelativeLayout relativeLayout = findViewById(R.id.Layout_relative);
        Log.e("width", ""+relativeLayout.getWidth());

        cropImageView.setAspectRatio(5, 10);
        cropImageView.setFixedAspectRatio(false);
        cropImageView.setGuidelines(1);

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
                Log.e("cord", cropX + " " + cropY + " " + cropWidth + " " + cropHeight);
                x = cropX;
                y = cropY;
                width = cropWidth;
                height = cropHeight;
                coordinates = "(" + cropX + ", " + cropY + ", " + cropWidth + ", " + cropHeight + ")";
            }
        });

        //get cropped image
        Button btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = cropImageView.getCroppedImage();
                Intent intent = new Intent(CropActivity.this, ResultActivity.class);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                byte[] bitmapByte = out.toByteArray();
                intent.putExtra("bp", bitmapByte);
                intent.putExtra("coordinates", coordinates);
                intent.putExtra("x", x);
                intent.putExtra("y", y);
                intent.putExtra("width", width);
                intent.putExtra("height", height);
                startActivity(intent);
            }
        });

    }
}