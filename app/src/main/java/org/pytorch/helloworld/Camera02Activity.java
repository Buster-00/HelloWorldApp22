package org.pytorch.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class Camera02Activity extends CameraActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cfg.setOnCheckClass(Test02Activity.class);
        getWindow().setStatusBarColor(getResources().getColor(R.color.white_translucent));//设置状态栏颜色
        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}