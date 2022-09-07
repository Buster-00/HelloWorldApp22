package camera;

import static camera.CameraParam.getBitmapDegree;
import static camera.CameraParam.rotateBitmapByDegree;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.camerax.lib.CameraFragment;

import org.pytorch.helloworld.PostProcessActivity;
import org.pytorch.helloworld.TestActivity;

import com.camerax.lib.R;

import java.io.FileNotFoundException;
import java.io.IOException;

/*
    default size:4000x2250px
 */

public class mCameraFragment extends CameraFragment {

    //Define KEY
    public final static String PICTURE_1 = "picture01";
    public final static String PICTURE_2 = "picture02";

    ImageView mCheckBtn;

    Bundle mbundle;

    int pic_counter = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        //set check button
        view.findViewById(R.id.check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mbundle.getString(PICTURE_1) != null && mbundle.getString(PICTURE_2) != null)
                {
                    Intent intent = new Intent(getActivity(), TestActivity.class);
                    intent.putExtras(mbundle);
                    startActivity(intent);
                    getActivity().finish();
                }
                else
                {
                    Toast.makeText(getActivity(), "need take two pictures.", Toast.LENGTH_LONG).show();
                }

            }
        });

        //Initialize bundle
        mbundle = new Bundle();

        return view;
    }

    @Override
    public void onSwitchCamera(boolean front) {
        getActivity().recreate();
    }

    @Override
    public void onTaken(Uri uri) {
        super.onTaken(uri);
        //返回拍照图片uri
        Log.e("onTake", "take photo");
        Toast.makeText(getActivity(), "take one picture", Toast.LENGTH_SHORT).show();

        if(pic_counter == 0)
        {
            //display the picture
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri));

                Matrix matrix = new Matrix();
                matrix.setScale(0.3f,0.3f);

                //set the bitmap
                bitmap = rotateBitmapByDegree(bitmap, getBitmapDegree(uri.getPath()));
                bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                ImageView img_view_1 = getActivity().findViewById(org.pytorch.helloworld.R.id.img_view_1);
                img_view_1.setImageBitmap(bitmap);

                //store the uri.toString to bundle
                mbundle.putString(PICTURE_1, uri.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            pic_counter++;
        }
        else if(pic_counter == 1)
        {
            //display the picture
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri));

                Matrix matrix = new Matrix();
                matrix.setScale(0.3f,0.3f);
                bitmap = rotateBitmapByDegree(bitmap, getBitmapDegree(uri.getPath()));
                bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

                ImageView img_view_2 = getActivity().findViewById(org.pytorch.helloworld.R.id.img_view_2);
                img_view_2.setImageBitmap(bitmap);
                mbundle.putString(PICTURE_2, uri.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            pic_counter++;

            //startActivity(new Intent(Camera2Activity.this, SelectActivity.class));
        }
        else
        {
            //TODO
        }

    }

    @Override
    public void onCancel() {
        super.onCancel();
        getActivity().finish();
    }
}
