package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.camerax.lib.CameraConstant;
import com.camerax.lib.core.CameraOption;
import com.camerax.lib.core.ExAspectRatio;
import com.camerax.lib.core.OnCameraListener;

import java.io.FileNotFoundException;
import java.io.IOException;

import camera.mCameraFragment;


public class CameraActivity extends AppCompatActivity {

    //Bundle to store the Uri


    //widget
    ImageView img_view_1;
    ImageView img_view_2;
    
    //picture taken counter
    int pic_counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Bundle mBundle = new Bundle();
        //Initialize widget
        img_view_1 = findViewById(R.id.img_view_1);
        img_view_2 = findViewById(R.id.img_view_2);

        //get fragment manager
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        final mCameraFragment cfg = new mCameraFragment();

        CameraOption option = new CameraOption.Builder(ExAspectRatio.RATIO_16_9)
                .faceFront(false)
                .build();

        Bundle data = new Bundle();
        data.putSerializable(CameraConstant.KEY_CAMERA_OPTION, option);
        cfg.setArguments(data);
        cfg.setOnCameraListener(new OnCameraListener() {
            @Override
            public void onTaken(Uri uri) {
                //返回拍照图片uri
                Log.e("onTake", "take photo");

                if(pic_counter == 0)
                {
                    //display the picture
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

                        Matrix matrix = new Matrix();
                        matrix.setScale(0.3f,0.3f);

                        bitmap = rotateBitmapByDegree(bitmap, getBitmapDegree(uri.getPath()));
                        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                        img_view_1.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    pic_counter++;
                }
                else if(pic_counter == 1)
                {
                    //display the picture
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

                        Matrix matrix = new Matrix();
                        matrix.setScale(0.3f,0.3f);
                        bitmap = rotateBitmapByDegree(bitmap, getBitmapDegree(uri.getPath()));
                        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                        img_view_2.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    pic_counter++;

                   //startActivity(new Intent(Camera2Activity.this, SelectActivity.class));
                }
                
               
            }

            @Override
            public void onCancel() {
                finish();
            }
        });

        //turn to camera fragment
        ft.replace(R.id.frg_1, cfg).commit();
    }

    //get the bitmap rotation degree
    static public int getBitmapDegree(String path)
    {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return degree;
    }

    //rotate the bitmap
    static public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

        if (returnBm == null) {
            returnBm = bm;
        }

        if(bm != returnBm)
        {
            bm.recycle();
        }
        return returnBm;

    }
}