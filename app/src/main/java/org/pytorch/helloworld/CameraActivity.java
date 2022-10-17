package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.camerax.lib.CameraConstant;
import com.camerax.lib.core.CameraOption;
import com.camerax.lib.core.ExAspectRatio;

import camera.mCameraFragment;


public class CameraActivity extends AppCompatActivity {

    //bundle to transfer uri.toString
    protected Bundle mbundle;

    //widget
    ImageView img_view_1;
    ImageView img_view_2;

    //Fragment
    mCameraFragment cfg;

    //picture taken counter
    int pic_counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //hide status bar
        //getWindow().setStatusBarColor(getResources().getColor(R.color.white_translucent));//设置状态栏颜色
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //create bundle to transfer uri.toString
        mbundle = new Bundle();

        //Initialize widget
        img_view_1 = findViewById(R.id.img_view_1);
        img_view_2 = findViewById(R.id.img_view_2);

        //get fragment manager
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        cfg = new mCameraFragment(false);
        cfg.setOnCheckClass(ImageProc01Activity.class);

        CameraOption option = new CameraOption.Builder(ExAspectRatio.RATIO_16_9)
                .faceFront(false)
                .build();

        Bundle data = new Bundle();
        data.putSerializable(CameraConstant.KEY_CAMERA_OPTION, option);
        cfg.setArguments(data);

        //turn to camera fragment
        ft.replace(R.id.frg_1, cfg).commit();




    }

}