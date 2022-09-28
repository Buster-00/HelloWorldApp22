package helper;

import static org.opencv.core.Core.add;
import static org.opencv.core.Core.divide;
import static org.opencv.core.Core.multiply;
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

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ssimHelper {
    static public void ssim(Mat img1, Mat img2){

        double C1 = 6.5025;
        double C2 = 58.5225;

        //Get the multiple result
        Mat img1_2 = img1.mul(img1);
        Mat img2_2 = img2.mul(img2);
        Mat img1_img2 = img1.mul(img2);

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
        multiply(mu1_mu2, new Scalar(2), t1);
        add(t1, new Scalar(C1), t1);

        //t2 = 2 * sigma12 + C2
        Mat t2 = new Mat();
        multiply(sigma12, new Scalar(2), t2);
        add(t2, new Scalar(C2), t2);

        //t3 = t1 * t2
        Mat t3 = new Mat();
        multiply(t1, t2, t3);

        //t1 = mu1_2 + mu2_2 + C1
        add(mu1_2, mu2_2, t1);
        add(t1, new Scalar(C1), t1);

        //t2 = sigma1_2 + sigma2_2 + C2
        add(sigma1_2, sigma2_2, t2);
        add(t2, new Scalar(C2), t2);

        // t1 = t1 * t2
        multiply(t1, t2, t1);

        //ssim_map = cv.divide(t3, t1)
        Mat ssim_map = new Mat();
        divide(t3, t1, ssim_map);

        //convert ssim_map to gray image
        Imgproc.cvtColor(ssim_map, ssim_map, COLOR_BGR2GRAY);

        //get difference    diff = (ssim_map_gray * 255).astype("uint8")
        Mat diff = new Mat();
        multiply(ssim_map, new Scalar(255), diff);

        //threshold
        Mat thresh = new Mat();
        threshold(diff, thresh, 100, 255,THRESH_BINARY_INV);

        //get contours
        List<MatOfPoint> cnts = new ArrayList<>();
        findContours(thresh, cnts, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_NONE);

        for(MatOfPoint contour : cnts){
            Rect rect = boundingRect(contour);
            rectangle(img1, rect, new Scalar(0,255,0), 4);
            rectangle(img2, rect, new Scalar(0,255,0), 4);
        }


    }
}
