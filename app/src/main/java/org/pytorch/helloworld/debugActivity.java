package org.pytorch.helloworld;

import static org.pytorch.helloworld.MainActivity.registration;
import static org.pytorch.helloworld.MainActivity.stringFromJNI;
import static org.pytorch.helloworld.MainActivity.testForNumcpp;
import static org.pytorch.helloworld.MainActivity.validate;
import static org.pytorch.helloworld.Param.HEIGHT;
import static org.pytorch.helloworld.Param.HEIGHT_OF_BITMAP;
import static org.pytorch.helloworld.Param.MODULE_NAME;
import static org.pytorch.helloworld.Param.WIDETH_OF_BITMAP;
import static org.pytorch.helloworld.Param.WIDTH;
import static camera.mCameraFragment.PICTURE_1;
import static camera.mCameraFragment.PICTURE_2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.opencv.imgproc.Imgproc;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import camera.CameraParam;

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

        //show process bar
        showProcessBar();

        //create a new thread to process task
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                processFunc();
                progressDialog.dismiss();
            }
        });

    }

    private void processFunc()
    {
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
            bitmap_import = BitmapFactory.decodeStream(getAssets().open("TestSample_3.jpg"));
            //Resize the bitmap
            bitmap_import = Bitmap.createScaledBitmap ( bitmap_import, n*WIDTH , n*HEIGHT , true ) ;
            imgHL1 = new Mat();
            Utils.bitmapToMat(bitmap_import,imgHL1);
            Mat imgHl1_2 = new Mat();
            org.opencv.imgproc.Imgproc.cvtColor(imgHL1, imgHl1_2, Imgproc.COLOR_RGBA2GRAY);

            //process second image
            bitmap_import = BitmapFactory.decodeStream(getAssets().open("TestSample_4.jpg"));

            //Resize the bitmap2
            bitmap_import = Bitmap.createScaledBitmap ( bitmap_import, n*WIDTH , n*HEIGHT , true ) ;
            imgHL2 = new Mat();
            Utils.bitmapToMat(bitmap_import,imgHL2);
        } catch (IOException e) {
            e.printStackTrace();
        }

//    imgRE1,imgRE2
//    int width = imgHL1_original.width();
//    int height = imgHL1_original.height();
        int width = imgHL1.width();
        int height = imgHL1.height();
        time_last += "\nre_begin:" + format.format(new Date());
        imgRE1 = new Mat();
        imgRE2 = new Mat();
//    Imgproc.cvtColor(imgHL1 , imgHL1 ,  COLOR_RGBA2RGB);
//    Imgproc.cvtColor(imgHL2 , imgHL2 ,  COLOR_RGBA2RGB);

        registration(imgHL1.getNativeObjAddr(), imgHL2.getNativeObjAddr(),imgRE1.getNativeObjAddr(),imgRE2.getNativeObjAddr());

        time_last += "\nre_end:" + format.format(new Date());

        // get imgRE1 and imgRE2 to yield to mask
        Bitmap img_out = Bitmap.createBitmap(imgRE1.cols(),imgRE1.rows(),Bitmap.Config.ARGB_8888);// = floatArrayToBitmap(scores,224,224,255);;
        Utils.matToBitmap(imgRE1,img_out);
        time_last += "\nmodel_begin:" + format.format(new Date());

        // use model to get the first mask
        img_out = Bitmap.createScaledBitmap ( img_out , WIDETH_OF_BITMAP , HEIGHT_OF_BITMAP , true) ;
        final Tensor inputTensor_1 = TensorImageUtils.bitmapToFloat32Tensor(img_out,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);//, MemoryFormat.CHANNELS_LAST

        final  IValue[] iValue_1=module.forward(IValue.from(inputTensor_1)).toTuple();
        final Tensor outputTensor_1 = iValue_1[0].toTensor();

        // getting tensor content as java array of floats
        final float[] tensor_array_1 = outputTensor_1.getDataAsFloatArray();
        Log.e("Length of float arrya", String.valueOf(tensor_array_1.length));

        //convert java array to Bitmap
        Bitmap bmp_mask_1=floatArrayToBitmap(tensor_array_1 ,WIDETH_OF_BITMAP,HEIGHT_OF_BITMAP,255);
        ImageView imageView = findViewById(R.id.image_view_1);
        imageView.setImageBitmap(bmp_mask_1);
        Log.e("BitmapSize", bmp_mask_1.getWidth() + " " + bmp_mask_1.getHeight());

        //second image
        img_out = Bitmap.createBitmap(imgRE2.cols(),imgRE2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgRE2,img_out);
//      ImageView imageView2 = findViewById(R.id.img_view_2);
//      imageView2.setImageBitmap(img_out);

        // use model to get the second mask
        img_out = Bitmap.createScaledBitmap ( img_out , WIDETH_OF_BITMAP , HEIGHT_OF_BITMAP , true ) ;
        final Tensor inputTensor_2 = TensorImageUtils.bitmapToFloat32Tensor(img_out,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);//, MemoryFormat.CHANNELS_LAST

        final  IValue[] iValue_2 = module.forward(IValue.from(inputTensor_2)).toTuple();
        final Tensor outputTensor_2 = iValue_2[0].toTensor();

        // getting tensor content as java array of floats
        final float[] tensor_array_2 = outputTensor_2.getDataAsFloatArray();

        //convert java array to Bitmap
        Bitmap bmp_mask_2=floatArrayToBitmap(tensor_array_2 ,WIDETH_OF_BITMAP,HEIGHT_OF_BITMAP,255);
        ImageView imageView2 = findViewById(R.id.img_view_2);
        imageView2.setImageBitmap(bmp_mask_2);
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
        ByteBuffer BB = ByteBuffer.allocate(512*384*4); //224*224*4
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