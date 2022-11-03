package org.pytorch.helloworld;

import static org.pytorch.helloworld.ImageProcBaseActivity.exposure_compensator;
import static org.pytorch.helloworld.MainActivity.registration;
import static org.pytorch.helloworld.MainActivity.stringFromJNI;
import static org.pytorch.helloworld.MainActivity.testForNumcpp;
import static org.pytorch.helloworld.MainActivity.validate;
import static helper.Param.HEIGHT;
import static helper.Param.HEIGHT_OF_BITMAP;
import static helper.Param.MODULE_NAME;
import static helper.Param.WIDTH_OF_BITMAP;
import static helper.Param.WIDTH;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import helper.GraphCutSeamFinderHelper;

public class debugActivity extends AppCompatActivity {

    //widget
    ProgressDialog progressDialog;

    //load JNI
    static {
        System.loadLibrary("HelloWorldApp");
        System.loadLibrary("opencv_java4");

    }

    private SIFT sift = SIFT.create();
    private FloatBuffer mInputTensorBuffer;
    private int[] inputArray= new int[224*224]  ;
    private Mat imgHL1_original, imgHL2_original, imgHL1,imgHL2,imgRE1,imgRE2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        getWindow().setStatusBarColor(getResources().getColor(R.color.white_translucent));//设置状态栏颜色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //show process bar
        showProcessBar();

        //create a new thread to process task
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    processFunc();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        });

    }

    private void processFunc() throws IOException {
        boolean success = OpenCVLoader.initDebug();
        String xx =stringFromJNI();
        String yy =validate(3,5);
        String zz =testForNumcpp();
        Bitmap bitmap = null;
        Module module = null;

        //Load high light detection model
        try {
            module = LiteModuleLoader.load(assetFilePath(this, MODULE_NAME));
            Log.e("PytorchHelloWorld", "model.pt");
        } catch (IOException e) {
            Log.e("PytorchHelloWorld", "Error reading assets", e);
            finish();
        }

        /* ----make second mask*/

        Bitmap bitmap_import= null;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String time_last= "timestamp:" + format.format(new Date());

        try {
            //define n = 1
            int n=1;

            /*import image from uri*/
            bitmap_import = BitmapFactory.decodeStream(getAssets().open("img_HL_1.jpg"));
            //Resize the bitmap
            bitmap_import = Bitmap.createScaledBitmap ( bitmap_import, n*WIDTH , n*HEIGHT , true ) ;
            imgHL1 = new Mat();
            Utils.bitmapToMat(bitmap_import,imgHL1);
            Mat imgHl1_2 = new Mat();
            org.opencv.imgproc.Imgproc.cvtColor(imgHL1, imgHl1_2, Imgproc.COLOR_RGBA2GRAY);

            //process second image
            bitmap_import = BitmapFactory.decodeStream(getAssets().open("img_HL_2.jpg"));

            //Resize the bitmap2
            bitmap_import = Bitmap.createScaledBitmap ( bitmap_import, n*WIDTH , n*HEIGHT , true ) ;
            imgHL2 = new Mat();
            Utils.bitmapToMat(bitmap_import,imgHL2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        imgRE1 = new Mat();
        imgRE2 = new Mat();

        registration(imgHL1.getNativeObjAddr(), imgHL2.getNativeObjAddr(),imgRE1.getNativeObjAddr(),imgRE2.getNativeObjAddr());

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.cvtColor(imgHL2, imgHL2, Imgproc.COLOR_RGB2GRAY);
        Imgproc.findContours(imgHL2,contours, new Mat(),Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Point points[] = contours.get(0).toArray();
        Rect roi = new Rect(points[0], points[2]);
        Mat imgRE1_crop_ = new Mat(imgRE1, roi);
        Mat imgRE2_crop_ = new Mat(imgRE2, roi);

        //Exposure compensator
        exposure_compensator(imgRE1_crop_.getNativeObjAddr(), imgRE2_crop_.getNativeObjAddr());

//        //Graph cut process
//        Imgproc.cvtColor(imgRE1_crop_, imgRE1_crop_, Imgproc.COLOR_BGRA2BGR);
//        Imgproc.cvtColor(imgRE2_crop_, imgRE2_crop_, Imgproc.COLOR_BGRA2BGR);
//
//
//        Mat result = GraphCutSeamFinderHelper.GraphCutSeamFinder(imgRE1_crop_, imgRE2_crop_);
//        result.convertTo(result, CvType.CV_8U, 255.0);
//
//        Bitmap bm = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.RGB_565);
//        Utils.matToBitmap(result,bm);
//
//        ImageView imageView = findViewById(R.id.image_view_1);
//        imageView.setImageBitmap(bm);

    }
    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private Bitmap floatArrayToBitmap(float[] floatArray, int width, int height, int alpha) {

        // Create empty bitmap in RGBA format
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] pixels = new byte[(width * height*4)];
        alpha = 255;

        // mapping smallest value to 0 and largest value to 255
//    Arrays.sort(floatArray);
        float maxValue = maximum(floatArray);// float)Collections.max(Arrays.asList(floatArray)) ? : 1.0f
        float minValue = minimum(floatArray);
        float delta = maxValue-minValue;

        // Define if float min..max will be mapped to 0..255 or 255..0

        // copy each value from float array to RGB channels and set alpha channel
        for (int i=0; i<width * height; ++i) {
            int tempValue=conversion(floatArray[i],minValue, delta);
//      int tempValue=0;

            pixels[i*4]=(byte)(tempValue&0xff);
            pixels[i*4+1]=(byte)(tempValue&0xff);
            pixels[i*4+2]=(byte)(tempValue&0xff);
            pixels[i*4+3]=(byte)(alpha&0xff);
        }
//    bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        ByteBuffer BB = ByteBuffer.allocate(WIDTH_OF_BITMAP * HEIGHT_OF_BITMAP * 4); //224*224*4
        BB.put(pixels);
        BB.position(0);
        bmp.copyPixelsFromBuffer(BB);

        return bmp;
    }
    private  int conversion(float v, float minValue, float delta){
        return Math.round(((v-minValue)/delta*255.0f));
    }

    public float maximum(float[] array) {
        if (array.length <= 0)
            throw new IllegalArgumentException("The array is empty");
        float max = array[0];
        for (int i = 1; i < array.length; i++)
            if (array[i] > max)
                max = array[i];
        return max;
    }

    public float minimum(float[] array) {
        if (array.length <= 0)
            throw new IllegalArgumentException("The array is empty");
        float min = array[0];
        for (int i = 1; i < array.length; i++)
            if (array[i] < min)
                min = array[i];
        return min;
    }

    public Bitmap sift(Bitmap inputImage) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(inputImage, rgba);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGBA2GRAY);
        sift.detect(rgba, keyPoints);
        Features2d.drawKeypoints(rgba, keyPoints, rgba);
        Utils.matToBitmap(rgba, inputImage);
//    imageView.setImageBitmap(inputImage);

//    import org.pytorch.torchvision.TensorImageUtils;//

//    TensorImageUtils.bitmapToFloat32Tensor()
        return inputImage ;
    }

    private void showProcessBar()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing the image");
        progressDialog.show();
    }
}