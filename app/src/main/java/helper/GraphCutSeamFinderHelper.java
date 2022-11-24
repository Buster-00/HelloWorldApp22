package helper;

import static org.opencv.photo.Photo.MIXED_CLONE;
import static org.opencv.photo.Photo.seamlessClone;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class GraphCutSeamFinderHelper {
    private static native void GraphCutSeamFinder_C(long img1_addr, long img2_addr, long resultd_addr, long mask_addr, int x, int y, int LENGTH);

    public static Mat[] GraphCutSeamFinder(Mat img1, Mat img2, int x, int y, int LENGTH){
        Mat result = new Mat();
        Mat mask = new Mat();

        //using native function graphcutSeamFinder
        GraphCutSeamFinder_C(img1.getNativeObjAddr(), img2.getNativeObjAddr(), result.getNativeObjAddr(), mask.getNativeObjAddr(), x, y, LENGTH);

        //seamless clone for the result
        Point center = new Point(x + (LENGTH / 2), y + (LENGTH / 2));
        seamlessClone(img2, img1, mask, center, result, MIXED_CLONE);

        Mat[] results = new Mat[2];
        results[0] = result;
        results[1] = mask;
        return results;
    }

    public static Mat GraphCutSeamFinderMask(Mat img1, Mat img2, int x, int y, int LENGTH){
        Mat mask = new Mat();
        GraphCutSeamFinder_C(img1.getNativeObjAddr(), img2.getNativeObjAddr(), new Mat().getNativeObjAddr(), mask.getNativeObjAddr(),  x, y, LENGTH);
        return mask;
    }
}
