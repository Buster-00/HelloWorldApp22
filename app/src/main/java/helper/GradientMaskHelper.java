package helper;

import static org.opencv.photo.Photo.MIXED_CLONE;
import static org.opencv.photo.Photo.seamlessClone;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class GradientMaskHelper {

    private static int STRENGTH = 800;

    //增加图像模糊遮罩
    private static Mat add_mask_xy(Mat img, int centerX, int centerY, int strength, int radius){
        int rows = img.rows();
        int cols = img.cols();

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){

                //计算当前点到遮罩中心距离
                double distance = pow((centerY - i), 2) + pow((centerX - j), 2);

                //获取原始图像
                byte [] currentPixelData = new byte[3];
                img.get(i, j, currentPixelData);

                //如果距离小于圆的半径
                if(distance < radius * radius){
                    //按照距离大小计算增强的遮罩值
                    int val = (int)(strength * (1.0 - (sqrt(distance) / radius)));

                    //获取BGR数据，将其转为int
                    int B = (int) currentPixelData[0];
                    int G = (int) currentPixelData[1];
                    int R = (int) currentPixelData[2];
                    B += val;
                    G += val;
                    R += val;

                    //防止越界
                    currentPixelData[0] = (byte) min(255, max(0, B));
                    currentPixelData[1] = (byte) min(255, max(0, G));
                    currentPixelData[2] = (byte) min(255, max(0, R));
                    img.put(i, j, currentPixelData);
                }
            }
        }

        return img;
    }

    public static Mat GradientMaskMaker(Mat img1, Mat img2, int CenterX, int CenterY){
        //加上遮罩
        return add_mask_xy(img1, CenterX, CenterY, 200, 50);
    }

    public static Mat GenerateMask(Mat imgRE1, Mat imgRE2, int rows, int cols, int CenterX, int CenterY, int radius){
        Mat imgRE1_ = new Mat(imgRE1.rows(), imgRE1.cols(), CvType.CV_8UC3);
        imgRE1.copyTo(imgRE1_);

        //生成一个渐变mask
        Mat temp = new Mat(rows, cols, CvType.CV_8UC3, new Scalar(0,0,0));
        Mat mask = add_mask_xy(temp, CenterX, CenterY, STRENGTH, radius);

        Point center = new Point(CenterX, CenterY);
        Mat result = new Mat(imgRE1.rows(), imgRE1.cols(), CvType.CV_8UC3);
        seamlessClone(imgRE2, imgRE1, mask, center, result, MIXED_CLONE);
        return result;
    }
}
