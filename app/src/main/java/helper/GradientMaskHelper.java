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

    private static int STRENGTH = 500;

    //增加图像模糊遮罩
    private static Mat add_mask_xy(Mat img, int centerX, int centerY, int strength, int radius){
        int rows = img.rows();
        int cols = img.cols();

        //获取图像数据
//        int numChannels = img.channels();
//        int frameSize = img.rows() * img.cols();
//        byte[] buffer = new byte[numChannels*frameSize];
//        img.get(0,0, buffer);

//        byte[][] out=new byte[frameSize][numChannels];
//        for (int p=0,i = 0; p < frameSize; p++) {
//            for (int n = 0; n < numChannels; n++,i++) {
//                out[p][n]= (byte) (buffer[i]&0xff);
//            }
//        }

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
//                    out[i * cols + j][0] += result;
//                    out[i * cols + j][1] += result;
//                    out[i * cols + j][2] += result;

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

//        for (int p=0,i = 0; p < frameSize; p++) {
//            for (int n = 0; n < numChannels; n++,i++) {
//                buffer[i] = out[p][n];
//            }
//        }
//        img.put(0,0,buffer);

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
        Mat mat = new Mat(rows, cols, CvType.CV_8UC3, new Scalar(0,0,0));
        //Mat mask = add_mask_xy(mat, rows/2, cols/2, 200, 50);
        Mat mask = add_mask_xy(mat, CenterX, CenterY, STRENGTH, radius);

        //Point center = new Point(rows/2, cols/2);
        Point center = new Point(CenterX, CenterY);
        Mat result = new Mat(imgRE1.rows(), imgRE1.cols(), CvType.CV_8UC3);
        seamlessClone(imgRE2, imgRE1, mask, center, result, MIXED_CLONE);
        //imgRE2.copyTo(imgRE1_, mask);
        return result;
    }
}
