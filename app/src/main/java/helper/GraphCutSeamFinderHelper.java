package helper;

import org.opencv.core.Mat;

public class GraphCutSeamFinderHelper {
    private static native void GraphCutSeamFinder_C(long img1_addr, long img2_addr, long resultd_addr, int x, int y, int LENGTH);

    public static Mat GraphCutSeamFinder(Mat img1, Mat img2, int x, int y, int LENGTH){
        Mat result = new Mat();
        GraphCutSeamFinder_C(img1.getNativeObjAddr(), img2.getNativeObjAddr(), result.getNativeObjAddr(), x, y, LENGTH);
        return result;
    }
}
