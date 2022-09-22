package org.pytorch.helloworld;

import static org.pytorch.helloworld.MainActivity.registration;
import static org.pytorch.helloworld.MainActivity.stringFromJNI;
import static org.pytorch.helloworld.MainActivity.testForNumcpp;
import static org.pytorch.helloworld.MainActivity.validate;

import static camera.mCameraFragment.PICTURE_1;
import static camera.mCameraFragment.PICTURE_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.pytorch.helloworld.Param.HEIGHT;
import static org.pytorch.helloworld.Param.HEIGHT_OF_BITMAP;
import static org.pytorch.helloworld.Param.MODULE_NAME;
import static org.pytorch.helloworld.Param.WIDTH_OF_BITMAP;
import static org.pytorch.helloworld.Param.WIDTH;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import camera.CameraParam;

public class TestBaseActivity extends AppCompatActivity {

    //module
    protected Module module = null;

    //Thread
    Thread mThread;
    Handler mHandler;

    //Bitmap
    Bitmap bm;

    //widget
    ProgressDialog progressDialog;
    FloatingActionButton btn_confirm;

    //load JNI
    static {
        System.loadLibrary("HelloWorldApp");
        System.loadLibrary("opencv_java4");

    }

    private SIFT sift = SIFT.create();
    private FloatBuffer mInputTensorBuffer;
    private int[] inputArray= new int[224*224]  ;
    protected Mat imgHL1_original, imgHL2_original, imgHL1,imgHL2,imgRE1,imgRE2;
    protected Mat imgRE1_crop_2;
    protected Mat imgRE1_crop_1;
    protected SimpleDateFormat format;
    protected String time_last;

    //native funciton
    private native int[] Clip(long im2_small_addr, long im1_p_addr, long im2_p_addr, long im1_crop_addr, long im2_crop_addr);

    private native void exposure_compensator(long im1_p_addr, long im2_p_addr);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //show process bar
        showProcessBar();

        //bind onClick listener
        btn_confirm = findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bm != null){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    byte[] bytes = out.toByteArray();
                    Intent intent = new Intent(TestBaseActivity.this, CropActivity.class);
                    intent.putExtra("bp", bytes);
                    startActivity(intent);
                }
            }
        });

        //Define handler
        mHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1000)
                {
                    //progressDialog.dismiss();
                }
            }
        };

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                processFunc();
                mHandler.sendEmptyMessage(1000);
            }
        });
        mThread.start();

    }

    protected void processFunc()
    {
        boolean success = OpenCVLoader.initDebug();
        String xx =stringFromJNI();
        String yy =validate(3,5);
        String zz =testForNumcpp();
        Bitmap bitmap = null;


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
        format = new SimpleDateFormat("HH:mm:ss");
        time_last = "timestamp:" + format.format(new Date());

        try {
            //define n = 1
            int n=1;

            /*import image from uri*/
            String uriString_1 = getIntent().getExtras().getString(PICTURE_1);
            Uri uri_1 = Uri.parse(uriString_1);
            bitmap_import = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri_1));
            bitmap_import = CameraParam.fixBitmap(bitmap_import, uri_1.getPath());


            //Resize the bitmap
            bitmap_import = Bitmap.createScaledBitmap ( bitmap_import, n*WIDTH , n*HEIGHT , true ) ;
            imgHL1 = new Mat();
            Utils.bitmapToMat(bitmap_import,imgHL1);
            Mat imgHl1_2 = new Mat();
            org.opencv.imgproc.Imgproc.cvtColor(imgHL1, imgHl1_2, Imgproc.COLOR_RGBA2GRAY);

            //save the bitmap
            CameraParam.mSaveBitmap(bitmap_import, this);

            //process second image
            /*import image from uri*/
            String uriString_2 = getIntent().getExtras().getString(PICTURE_2);
            Uri uri_2 = Uri.parse(uriString_2);
            bitmap_import = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri_2));
            bitmap_import = CameraParam.fixBitmap(bitmap_import, uri_2.getPath());


            //Resize the bitmap2
            bitmap_import = Bitmap.createScaledBitmap ( bitmap_import, n*WIDTH , n*HEIGHT , true ) ;
            imgHL2 = new Mat();
            Utils.bitmapToMat(bitmap_import,imgHL2);
        } catch (IOException e) {
            e.printStackTrace();
        }

//      imgRE1,imgRE2
//      int width = imgHL1_original.width();
//      int height = imgHL1_original.height();
        int width = imgHL1.width();
        int height = imgHL1.height();
        time_last += "\nre_begin:" + format.format(new Date());
        imgRE1 = new Mat();
        imgRE2 = new Mat();
//      Imgproc.cvtColor(imgHL1 , imgHL1 ,  COLOR_RGBA2RGB);
//      Imgproc.cvtColor(imgHL2 , imgHL2 ,  COLOR_RGBA2RGB);

        //Registration
        registration(imgHL1.getNativeObjAddr(), imgHL2.getNativeObjAddr(),imgRE1.getNativeObjAddr(),imgRE2.getNativeObjAddr());

        //Clip
        Mat imgRE1_crop = new Mat();
        Mat imgRE2_crop = new Mat();
        
        int[] coordinates =  Clip(imgHL2.getNativeObjAddr(), imgRE1.getNativeObjAddr(), imgRE2.getNativeObjAddr(),imgRE1_crop.getNativeObjAddr(), imgRE2_crop.getNativeObjAddr());
        for(int i : coordinates){
            Log.e("array", " " + i);
        }
        Rect roi = new Rect(new Point(coordinates[2], coordinates[0]), new Point(coordinates[3], coordinates[1]));
        imgRE1_crop_1 = new Mat(imgRE1, roi);
        imgRE1_crop_2 = new Mat(imgRE2, roi);

        //Exposure compensator
        exposure_compensator(imgRE1_crop_1.getNativeObjAddr(), imgRE2_crop.getNativeObjAddr());



/*
//    Mat mat_mask1 = new Mat();
//    Mat mat_mask2 = new Mat();

//    Bitmap bm_mask_224=null;
//    try {
//      bm_mask_224  = BitmapFactory.decodeStream(getAssets().open("bw224.jpg"));
//    } catch (IOException e) {
//      Log.e("PytorchHelloWorld", "Error reading assets", e);
//      finish();
//    }

    time_last += "\nmodel_end:" + format.format(new Date());
    bmp_mask_1 = Bitmap.createScaledBitmap ( bmp_mask_1 , width , height , true ) ;
    bmp_mask_2 = Bitmap.createScaledBitmap ( bmp_mask_2 , width , height  , true ) ;

    Utils.bitmapToMat(bmp_mask_1,mat_mask1);
    Utils.bitmapToMat(bmp_mask_2,mat_mask2);

//    Imgproc.rectangle (
//            mat_mask1, //Matrix obj of the image
//            new Point(130, 50), //p1
//            new Point(220, 150), //p2
//            new Scalar(255, 255, 255), //Scalar object for color
//            -1 //Thickness of the line
//    );

    Bitmap tmp =  Bitmap.createBitmap(mat_mask1.cols(),mat_mask1.rows(),Bitmap.Config.ARGB_8888);

    Imgproc.cvtColor(mat_mask1 , mat_mask1 ,  COLOR_RGBA2RGB);
    Imgproc.cvtColor(imgRE1 , imgRE1 ,  COLOR_RGBA2RGB);
    Imgproc.cvtColor(imgRE2 , imgRE2 ,  COLOR_RGBA2RGB);

    Utils.matToBitmap(mat_mask1,tmp);
    time_last += "\nseam_begin:" + format.format(new Date());
    Mat mat_result = new Mat();
    seamlessclone(imgRE1.getNativeObjAddr(),
            imgRE2.getNativeObjAddr(),
            mat_mask1.getNativeObjAddr(),
            mat_mask2.getNativeObjAddr(),
            mat_result.getNativeObjAddr());
    time_last += "\nseam_end:" + format.format(new Date());
    Bitmap bmp_result = Bitmap.createBitmap(mat_result.cols(),mat_result.rows(),Bitmap.Config.ARGB_8888);// = floatArrayToBitmap(scores,224,224,255);;
    Utils.matToBitmap(mat_result,bmp_result);

    // showing image on UI

    imageView.setImageBitmap(bmp_result);
    TextView tx = findViewById(R.id.text);
    tx.setText(time_last);

    //create a file to write bitmap data
    File f = new File(getApplicationContext().getCacheDir(), "mask.png");
    try {
      f.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

//Convert bitmap to byte array
//    Bitmap bitmap = your bitmap;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bmp_result.compress(Bitmap.CompressFormat.PNG, 0 , bos);
    byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(f);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    try {
      fos.write(bitmapdata);
      fos.flush();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

//    // searching for the index with maximum score
//    float maxScore = -Float.MAX_VALUE;
//    int maxScoreIdx = -1;
//    for (int i = 0; i < scores.length; i++) {
//      if (scores[i] > maxScore) {
//        maxScore = scores[i];
//        maxScoreIdx = i;
//      }
//    }
//
//    String className = ImageNetClasses.IMAGENET_CLASSES[maxScoreIdx];
//
//    // showing className on UI
//    TextView textView = findViewById(R.id.text);
//    textView.setText(className);
 */
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

    protected Bitmap floatArrayToBitmap(float[] floatArray, int width, int height, int alpha) {

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
        ByteBuffer BB = ByteBuffer.allocate(WIDTH_OF_BITMAP*HEIGHT_OF_BITMAP*4); //512x384
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
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void onCompleteWrap()
    {

    }

}