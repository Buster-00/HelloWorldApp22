package org.pytorch.helloworld;

import static org.opencv.photo.Photo.fastNlMeansDenoising;
import static org.pytorch.helloworld.Param.HEIGHT_OF_BITMAP;
import static org.pytorch.helloworld.Param.WIDTH_OF_BITMAP;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.pytorch.IValue;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.util.Date;

public class ImageProc02Activity extends ImageProcBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    protected void postProcess() {
        time_last += "\nre_end:" + format.format(new Date());

        // get imgRE1 and imgRE2 to yield to mask
        Bitmap img_out = Bitmap.createBitmap(imgRE1.cols(),imgRE1.rows(),Bitmap.Config.ARGB_8888);// = floatArrayToBitmap(scores,224,224,255);;
        Utils.matToBitmap(imgRE1,img_out);
        time_last += "\nmodel_begin:" + format.format(new Date());

        // use model to get the first mask
        img_out = Bitmap.createScaledBitmap ( img_out , WIDTH_OF_BITMAP, HEIGHT_OF_BITMAP , true) ;
        final Tensor inputTensor_1 = TensorImageUtils.bitmapToFloat32Tensor(img_out,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);//, MemoryFormat.CHANNELS_LAST

        final  IValue[] iValue_1=module.forward(IValue.from(inputTensor_1)).toTuple();
        final Tensor outputTensor_1 = iValue_1[0].toTensor();

        // getting tensor content as java array of floats
        final float[] tensor_array_1 = outputTensor_1.getDataAsFloatArray();
        Log.e("Length of float arrya", String.valueOf(tensor_array_1.length));

        //convert java array to Bitmap
        Bitmap bmp_mask_1=floatArrayToBitmap(tensor_array_1 , WIDTH_OF_BITMAP,HEIGHT_OF_BITMAP,255);
        ImageView imageView = findViewById(R.id.image_view_1);

        Log.e("BitmapSize", bmp_mask_1.getWidth() + " " + bmp_mask_1.getHeight());

        //second image
        img_out = Bitmap.createBitmap(imgRE2.cols(),imgRE2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgRE2,img_out);
//      ImageView imageView2 = findViewById(R.id.img_view_2);
//      imageView2.setImageBitmap(img_out);

        // use model to get the second mask
        img_out = Bitmap.createScaledBitmap ( img_out , WIDTH_OF_BITMAP , HEIGHT_OF_BITMAP , true ) ;
        final Tensor inputTensor_2 = TensorImageUtils.bitmapToFloat32Tensor(img_out,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);//, MemoryFormat.CHANNELS_LAST

        final  IValue[] iValue_2 = module.forward(IValue.from(inputTensor_2)).toTuple();
        final Tensor outputTensor_2 = iValue_2[0].toTensor();

        // getting tensor content as java array of floats
        final float[] tensor_array_2 = outputTensor_2.getDataAsFloatArray();

        //convert java array to Bitmap
        Bitmap bmp_mask_2=floatArrayToBitmap(tensor_array_2 ,WIDTH_OF_BITMAP,HEIGHT_OF_BITMAP,255);
        ImageView imageView2 = findViewById(R.id.img_view_2);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bmp_mask_1);
                imageView2.setImageBitmap(bmp_mask_2);;
                progressDialog.dismiss();
            }
        });
    }


}