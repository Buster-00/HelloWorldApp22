package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.core.Mat;

public class ResultActivity extends AppCompatActivity {

    public native Mat user_mask_seamlessclone(long im1_p_addr, long im2_p_addr, long des_addr, int x, int y, int width, int height);

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

        if(bm != null)
        {
            imageView.setImageBitmap(bm);
        }

        if(!coordinates.isEmpty())
        {
            textView.setText(coordinates);
        }

    }
}