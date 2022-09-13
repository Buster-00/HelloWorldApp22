package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    //widget
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        byte[] bitmapByte = getIntent().getByteArrayExtra("bp");
        Bitmap bm = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
        String coordinates = getIntent().getStringExtra("coordinates");

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.tv_coordinate);

        imageView.setImageBitmap(bm);
        textView.setText(coordinates);
    }
}