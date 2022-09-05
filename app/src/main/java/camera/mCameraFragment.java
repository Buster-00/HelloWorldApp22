package camera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.camerax.lib.CameraFragment;

import org.pytorch.helloworld.Camera2Activity;
import com.camerax.lib.R;

public class mCameraFragment extends CameraFragment {

    ImageView mCheckBtn;

    @Override
    public void onSwitchCamera(boolean front) {
        getActivity().recreate();
    }
}
