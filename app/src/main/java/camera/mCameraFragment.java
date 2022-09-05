package camera;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.camerax.lib.CameraFragment;

import org.pytorch.helloworld.Camera2Activity;
import org.pytorch.helloworld.R;

public class mCameraFragment extends CameraFragment {
    @Override
    public void onSwitchCamera(boolean front) {
        getActivity().recreate();
    }
}
