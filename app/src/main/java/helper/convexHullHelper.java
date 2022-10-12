package helper;

import static org.opencv.core.Core.findNonZero;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.convexHull;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.photo.Photo.fastNlMeansDenoising;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class convexHullHelper {

    //detect the high light area
    public static Rect detectHighlightArea(Mat src) {

        Mat img = new Mat();
        src.copyTo(img);
        cvtColor(img, img, COLOR_BGR2GRAY);
        //bright threshold
        double bright_threshold = 225;

        //Denoising
        fastNlMeansDenoising(img, img);

        //Get a binary image out of a grayscale image
        Imgproc.threshold(img, img, bright_threshold, 255, Imgproc.THRESH_BINARY);

        MatOfPoint points = new MatOfPoint();
        findNonZero(img, points);

        MatOfInt hull = new MatOfInt();
        MatOfPoint hullPointMat = new MatOfPoint();
        ArrayList<Point> hullPointList = new ArrayList<>();

        Rect rect = new Rect(50,50,50,50);

        //perform convexHull
        if(!points.empty()){

            convexHull(points, hull, true);

            //add points to hullPointList
            for (int i = 0; i < hull.toList().size(); i++) {
                hullPointList.add(points.toList().get(hull.toList().get(i)));
            }

            hullPointMat.fromList(hullPointList);

            //get rectangle
            rect = boundingRect(hullPointMat);

            Log.e("rect", rect.toString());
        }

        return rect;
    }
}
