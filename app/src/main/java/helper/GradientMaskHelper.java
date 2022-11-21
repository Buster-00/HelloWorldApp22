package helper;

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

    //增加图像模糊遮罩特效
    private static Mat add_mask_xy(Mat img, int centerX, int centerY, int strength, int radius){
        int rows = img.rows();
        int cols = img.cols();

        //获取图像数据
        int numChannels = img.channels();
        int frameSize = img.rows() * img.cols();
        byte[] buffer = new byte[numChannels*frameSize];
        img.get(0,0, buffer);

        byte[][] out=new byte[frameSize][numChannels];
        for (int p=0,i = 0; p < frameSize; p++) {
            for (int n = 0; n < numChannels; n++,i++) {
                out[p][n]= (byte) (buffer[i]&0xff);
            }
        }

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){

                //计算当前点到遮罩中心距离
                double distance = pow((centerY - j), 2) + pow((centerX - i), 2);

                //获取原始图像
//                byte [] currentPixelData = new byte[(int) (img.total() * img.channels())];
//                img.get(i, j, currentPixelData);

                //如果距离小于圆的半径
                if(distance <= radius * radius){
                    //按照距离大小计算增强的遮罩值
                    int result = (int)(strength * (1.0 - sqrt(distance) / radius));
                    out[i * cols + j][0] += result;
                    out[i * cols + j][1] += result;
                    out[i * cols + j][2] += result;

                    //防止越界
//                    currentPixelData[0] = (byte) min(255, max(0, currentPixelData[0]));
//                    currentPixelData[1] = (byte) min(255, max(0, currentPixelData[1]));
//                    currentPixelData[2] = (byte) min(255, max(0, currentPixelData[2]));
//                    img.put(i, j, currentPixelData);
                }
            }
        }

        for (int p=0,i = 0; p < frameSize; p++) {
            for (int n = 0; n < numChannels; n++,i++) {
                buffer[i] = out[p][n];
            }
        }

        img.put(0,0,buffer);
        return img;
    }

    public static Mat GradientMaskMaker(Mat img1, Mat img2, int CenterX, int CenterY){

        //加上遮罩
        return add_mask_xy(img1, CenterX, CenterY, 200, 50);
    }

    public static Mat GenerateMask(Mat imgRE1, Mat imgRE2, int rows, int cols){
        //生成一个渐变mask
        Mat mat = new Mat(rows, cols, CvType.CV_8UC3, new Scalar(0,0,0));
        Mat mask = add_mask_xy(mat, rows/2, cols/2, 200, 50);

        Point center = new Point(rows/2, cols/2);
        Mat result = new Mat(imgRE1.rows(), imgRE1.cols(), CvType.CV_8UC3);
        seamlessClone(imgRE2, imgRE1, mask, center, result, 1);
        //imgRE2.copyTo(imgRE1, mask);
        return result;
    }
}
