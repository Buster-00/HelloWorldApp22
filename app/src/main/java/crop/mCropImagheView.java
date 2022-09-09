package crop;

import android.content.Context;
import android.graphics.Bitmap;

import com.edmodo.cropper.CropImageView;


public class mCropImagheView extends CropImageView {

    OnCropListener mOnCropListener;

    public mCropImagheView(Context context) {
        super(context);
    }

    @Override
    public Bitmap getCroppedImage() {
        Bitmap bm = super.getCroppedImage();
        return bm;
    }

    public void setOnCropListener(OnCropListener listener)
    {
        mOnCropListener = listener;
    }
}
