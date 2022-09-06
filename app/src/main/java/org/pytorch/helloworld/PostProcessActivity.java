package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileNotFoundException;

import camera.mCameraFragment;

public class PostProcessActivity extends AppCompatActivity {

    //widegt
    ImageView img_1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_process);

        //initialize widget
        img_1 = findViewById(R.id.img_1);

        //GET IMAGE URI
        Bundle bundle = getIntent().getExtras();

        //uri path of the picture
        String img_1_path = bundle.getString(mCameraFragment.PICTURE_1);
        Log.e("picture path", img_1_path);
        Uri uri = Uri.parse(img_1_path);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        img_1.setImageBitmap(bitmap);

    }
}