package helper;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import org.opencv.core.Mat;

public class MaskHelper {

    //增加图像模糊遮罩特效
    private Mat add_mask_xy(Mat img, int centerX, int centerY, int strength, int radius){
        int rows = img.rows();
        int cols = img.cols();

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){

                //计算当前点到遮罩中心距离
                double distance = pow((centerY - j), 2) + pow((centerX - centerX), 2);

                //获取原始图像
                int[] currentPixelData = new int[3];
                img.get(i, j, currentPixelData);

                //如果距离小于圆的半径
                if(distance <= radius * radius){
                    //按照距离大小计算增强的遮罩值
                    int result = (int)(strength * (1.0 - sqrt(distance) / radius));
                    currentPixelData[0] += result;
                    currentPixelData[1] += result;
                    currentPixelData[2] += result;

                    //防止越界
                    currentPixelData[0] = min(255, max(0, currentPixelData[0]));
                    currentPixelData[1] = min(255, max(0, currentPixelData[1]));
                    currentPixelData[2] = min(255, max(0, currentPixelData[2]));
                    img.put(i, j, currentPixelData);
                }
            }
        }
        return img;
    }
}
