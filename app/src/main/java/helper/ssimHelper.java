package helper;

import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.add;
import static org.opencv.core.Core.divide;
import static org.opencv.core.Core.multiply;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.Core.subtract;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.threshold;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ssimHelper {
    static public void ssim(Mat i1, Mat i2){

        double C1 = 6.5025;
        double C2 = 58.5225;

        Mat img1 = new Mat();
        Mat img2 = new Mat();

        i1.copyTo(img1);
        i2.copyTo(img2);
        //Get the multiple result
        img1.convertTo(img1, CvType.CV_32F,1/255.0);
        img2.convertTo(img2, CvType.CV_32F,1/255.0);
        Mat img1_2 = new Mat();
        multiply(img1, img1, img1_2);
        Mat img2_2 = new Mat();
        multiply(img2, img2, img2_2);
        Mat img1_img2 = new Mat();
        multiply(img1, img2, img1_img2);

        //Preliminary computing
        Mat mu1 = new Mat();
        Mat mu2 = new Mat();
        GaussianBlur(img1, mu1, new Size(11, 11), 1.5);
        GaussianBlur(img2, mu2, new Size(11, 11), 1.5);
        Mat mu1_2 = mu1.mul(mu1);
        Mat mu2_2 = mu2.mul(mu2);
        Mat mu1_mu2 = mu1.mul(mu2);

        Mat sigma1_2 = new Mat();
        Mat sigma2_2 = new Mat();
        Mat sigma12 = new Mat();
        GaussianBlur(img1_2, sigma1_2, new Size(11, 11), 1.5);
        subtract(sigma1_2, mu1_2, sigma1_2);
        GaussianBlur(img2_2, sigma2_2, new Size(11, 11), 1.5);
        subtract(sigma2_2, mu2_2, sigma2_2);
        GaussianBlur(img1_img2, sigma12, new Size(11, 11), 1.5);
        subtract(sigma12, mu1_mu2, sigma12);

        //t1 = 2 * mu1_mu2 + C1
        Mat t1 = new Mat();
        multiply(mu1_mu2, new Scalar(2,2,2), t1);
        add(t1, new Scalar(C1,C1,C1), t1);

        //t2 = 2 * sigma12 + C2
        Mat t2 = new Mat();
        multiply(sigma12, new Scalar(2,2,2), t2);
        add(t2, new Scalar(C2,C2,C2), t2);

        //t3 = t1 * t2
        Mat t3 = new Mat();
        multiply(t1, t2, t3);

        //t1 = mu1_2 + mu2_2 + C1
        add(mu1_2, mu2_2, t1);
        add(t1, new Scalar(C1,C1,C1), t1);

        //t2 = sigma1_2 + sigma2_2 + C2
        add(sigma1_2, sigma2_2, t2);
        add(t2, new Scalar(C2,C2,C2), t2);

        // t1 = t1 * t2
        multiply(t1, t2, t1);

        //ssim_map = cv.divide(t3, t1)
        Mat ssim_map = new Mat();
        divide(t3, t1, ssim_map);

        //convert ssim_map to gray image
        Imgproc.cvtColor(ssim_map, ssim_map, COLOR_BGR2GRAY);
        Mat diff = new Mat();

        //get difference    diff = (ssim_map_gray * 255).astype("uint8")
        ssim_map.convertTo(diff, CvType.CV_8U, 255.0);

        //save diff to bitmap
        Bitmap bitmap = Bitmap.createBitmap(diff.cols(), diff.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(diff, bitmap);
        try {
            FileOutputStream out = new FileOutputStream(new File("file:///storage/emulated/0/Android/data/org.pytorch.helloworld/files/Pictures/diff.jpg"));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //threshold
        Mat thresh = new Mat();
        threshold(diff, thresh, 247, 255,THRESH_BINARY_INV);

        //get contours
        List<MatOfPoint> cnts = new ArrayList<>();
        findContours(thresh, cnts, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_NONE);

        for(MatOfPoint contour : cnts){
            Rect rect = boundingRect(contour);
            rectangle(i1, rect, new Scalar(0,255,0), 4);
            rectangle(i2, rect, new Scalar(0,255,0), 4);
        }


    }
}
