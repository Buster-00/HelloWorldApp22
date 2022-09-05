package camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.camerax.lib.CameraFragment;

import org.pytorch.helloworld.PostProcessActivity;

import com.camerax.lib.R;

public class mCameraFragment extends CameraFragment {

    ImageView mCheckBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        view.findViewById(R.id.check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PostProcessActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onSwitchCamera(boolean front) {
        getActivity().recreate();
    }
}
