package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        byte[] bitmapByte = getIntent().getByteArrayExtra("bp");
        Bitmap bm = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(bm);
    }
}