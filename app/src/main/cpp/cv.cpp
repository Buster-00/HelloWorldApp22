//
// Created by JH on 2022/7/30.
//

#include<jni.h>
#include<string>

#include <bitset>
#include <vector>

#include"NumCpp.hpp"
#include "opencv2/core.hpp"
#include <opencv2/highgui/highgui.hpp>
#include<opencv2/imgcodecs/imgcodecs.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/calib3d.hpp>
#include "opencv2/photo/photo.hpp"
#include "opencv2/stitching/detail/exposure_compensate.hpp"
#include "opencv2/stitching/detail/seam_finders.hpp"


using namespace cv;
using namespace nc;
using namespace std;

extern "C"  JNIEXPORT jstring JNICALL
Java_org_pytorch_helloworld_MainActivity_stringFromJNI( JNIEnv *env, jobject thiz){
//    char *hello = "hello test...";
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());

}

int max(int x, int y){
    return  x>y?x:y;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_org_pytorch_helloworld_MainActivity_validate(JNIEnv *env, jobject thiz, jint mad_addr_gr,jint mat_addr_rgba) {
    cv::Rect();
    cv::Mat();
    std::string hello2="hello from validate";
    return env->NewStringUTF(hello2.c_str());
    // TODO: implement validate()
}
extern "C"  JNIEXPORT jstring JNICALL
Java_org_pytorch_helloworld_MainActivity_testForNumcpp(
        JNIEnv *env,
        jobject thiz){
//    char *hello = "hello test...";
    nc::NdArray<int> a={{7,2},{3,4},{5,6}};
    std::string hello = std::to_string(max(a(0,0),a(0,1)))+"from numcpp";
    return env->NewStringUTF(hello.c_str());

}


//////////////////////////////////////////////////////////

//#include <opencv.hpp>

//#include "opencv2/xfeatures2d.hpp"
//#include <NumCpp.hpp>
//#include <vector>

using namespace cv;
using namespace nc;
using namespace std;

void mosaic_global_v1(Mat &im1, Mat &im2, nc::NdArray<double> &H, Mat &im1_p, Mat &im2_p);

void load_data(Mat &im1, Mat &im2, nc::NdArray<double> & fp, nc::NdArray<double> & tp)
{
    //Mat im1_gray, im2_gray;
    //cv::cvtColor(im1,im1_gray,cv::COLOR_BGR2GRAY);
    //cv::cvtColor(im2, im2_gray, cv::COLOR_BGR2GRAY);
    Ptr<SIFT> detector = SIFT::create();

    // Detect features and compute descriptors
    Mat descriptors_object, descriptors_scene;
    vector<KeyPoint> keypoints_object;
    vector<KeyPoint> keypoints_scene;


    detector->detectAndCompute(im1, noArray(), keypoints_object, descriptors_object);
    detector->detectAndCompute(im2, noArray(), keypoints_scene, descriptors_scene);

    // Match descriptors using FLANN
    //FlannBasedMatcher matcher;
    //vector< vector<DMatch> > matches;
    //matcher.knnMatch(descriptors_object, descriptors_scene, matches, 2);
    //	vector<DMatch> good_matches;
    //if (matches.size() <= 4) {
    //	cout << "Error: too few matches were found" << endl;
    //}
    //else
    //{
    //	for (int i = 0; i < (int)matches.size(); i++) //THIS LOOP IS SENSITIVE TO SEGFAULTS
    //	{
    //		if ((matches[i][0].distance < 0.7*(matches[i][1].distance)) && ((int)matches[i].size() <= 2 && (int)matches[i].size() > 0))
    //		{
    //			good_matches.push_back(matches[i][0]);
    //		}
    //	}
    //	if (good_matches.size() >= 10)
    //	{
    //		fp = nc::NdArray<double>(3, good_matches.size());
    //		tp = nc::NdArray<double>(3, good_matches.size());
    //		//vector<nc::NdArray<double>> result;
    //		for (int j = 0; j < (int)good_matches.size(); j++)
    //		{
    //			fp(0, j) = keypoints_object[good_matches[j].queryIdx].pt.x;
    //			fp(1, j) = keypoints_object[good_matches[j].queryIdx].pt.y;
    //			fp(2, j) = 1;
    //			//cout << "a1:\n" << fp[j,0] << endl;
    //			//cout << "a1:\n" << fp[j,1] << endl;
    //			//cout << "a1:\n" << fp[j,2] << endl;

    //			tp(0, j) = keypoints_scene[good_matches[j].trainIdx].pt.x;
    //			tp(1, j) = keypoints_scene[good_matches[j].trainIdx].pt.y;
    //			tp(2, j) = 1;
    //		}
    //		//arr.push_back(fp);
    //		//arr.push_back(tp);


    //		//cout << "a2:\n" << fp << endl;
    //		//return arr;
    //	}
    //}
    ////printf("draw good matches\n");
    ////Show detected matches

    // Match using BF
    BFMatcher matcher;
    std::vector<vector<DMatch >> matches;
    matcher.knnMatch(descriptors_object, descriptors_scene, matches, 2);

    std::vector<DMatch> good_matches;
    for (int i = 0; i < matches.size(); i++) {
        float rejectRatio = 0.7;
        if (matches[i][0].distance / matches[i][1].distance > rejectRatio)
            continue;
        good_matches.push_back(matches[i][0]);
    }

    std::vector<Point2f> good_keyPoints1, good_keyPoints2;
    for (int i = 0; i < good_matches.size(); i++) {
        good_keyPoints1.push_back(keypoints_object[good_matches[i].queryIdx].pt);
        good_keyPoints2.push_back(keypoints_scene[good_matches[i].trainIdx].pt);
    }

    Mat status;
    Mat H = findHomography(good_keyPoints1, good_keyPoints2, 4.0, RANSAC, status);


    auto inlines = cv::sum(status);
    fp = nc::NdArray<double>(3, int(inlines[0]));
    tp = nc::NdArray<double>(3, int(inlines[0]));
    //vector<nc::NdArray<double>> result;
    int k = 0;
    const int W = status.cols, h = status.rows;

    //cout << "a1:\n" << status<< endl;
    Mat_<uchar>::iterator it1 = status.begin<uchar>();
    Mat_<uchar>::iterator itend1 = status.end<uchar>();
    int j = 0;
    for (; it1 != itend1; ++it1) {
        //(*it1) = 128;
        //cout << "a1:\n" << int((*it1)) << endl;
        if (int((*it1)) == 1) {
            fp(0, k) = good_keyPoints1[j].x;
            fp(1, k) = good_keyPoints1[j].y;
            fp(2, k) = 1;
            //cout << "a1:\n" << fp[j,0] << endl;
            //cout << "a1:\n" << fp[j,1] << endl;
            //cout << "a1:\n" << fp[j,2] << endl;

            tp(0, k) = good_keyPoints2[j].x;
            tp(1, k) = good_keyPoints2[j].y;
            tp(2, k) = 1;
            k = k + 1;
        }
        j = j + 1;
    }

    //for (int j = 0; j < (int)good_matches.size(); j++)
    //{

    //	if (status.at<uchar>(0, j) == 1) {
    //		fp(0, k) = good_keyPoints1[j].x;
    //		fp(1, k) = good_keyPoints1[j].y;
    //		fp(2, k) = 1;
    //		//cout << "a1:\n" << fp[j,0] << endl;
    //		//cout << "a1:\n" << fp[j,1] << endl;
    //		//cout << "a1:\n" << fp[j,2] << endl;

    //		tp(0, k) = good_keyPoints2[j].x;
    //		tp(1, k) = good_keyPoints2[j].y;
    //		tp(2, k) = 1;
    //		k = k + 1;
    //	}
    //}

    //cout << "fp:\n" << fp << endl;
    //cout << "tp:\n" << tp << endl;

    //Mat rect;
    //drawMatches(im1, keypoints_object, im2, keypoints_scene, good_matches,rect);
    //imshow("keypoints", rect);
    //waitKey(0);
    //destroyAllWindows();



    keypoints_object.clear();
    keypoints_scene.clear();
    matches.clear();
    good_matches.clear();

}

nc::NdArray<double> HM_ransac(nc::NdArray<double> & fp, nc::NdArray<double> & tp, int Nr, double min_dis)
{
    int N = fp.shape().cols;

    nc::NdArray<double> u = fp(0, fp.cSlice()).reshape(-1, 1);
    nc::NdArray<double> v = fp(1, fp.cSlice()).reshape(-1, 1);
    nc::NdArray<double> u_ = tp(0, fp.cSlice()).reshape(-1, 1);
    nc::NdArray<double> v_ = tp(1, fp.cSlice()).reshape(-1, 1);
    nc::NdArray<double>  scale = 1.0 / nc::mean(nc::vstack({ u, u_, v, v_ }));
    double scale_value = scale.at(0);

    u = u * scale_value;

    v = v * scale_value;
    u_ = u_ * scale_value;
    v_ = v_ * scale_value;

    auto A1 = nc::hstack({ nc::zeros<double>(N, 3),-u,-v,-nc::ones<double>(N, 1),v_*u, v_*v, v_ });

    auto A2 = nc::hstack({ u,v,nc::ones<double>(N, 1),nc::zeros<double>(N, 3), -u_ * u, -u_ * v, -u_ });

    if (min_dis > 0)
    {
        nc::NdArray<double> * A = new nc::NdArray<double>[Nr];
        int best_score = -1;
        int best_arg = -1;
        nc::NdArray<bool>  best_ok;

        for (size_t i = 0; i < Nr; i++)
        {
            auto subset = nc::random::choice(nc::arange<int>(N), 4);

            A[i] = nc::vstack({ A1(subset, A1.cSlice()),A2(subset, A1.cSlice()) });

            nc::NdArray<double> U;
            nc::NdArray<double> s;
            nc::NdArray<double> vt;
            nc::linalg::svd(A[i], U, s, vt);

            auto vt_T = nc::transpose(vt);
            auto h = vt_T(vt_T.rSlice(), 8);

            auto dis2 = nc::power(nc::dot(A1, h), 2) + nc::power(nc::dot(A2, h), 2);

            auto ok_t = dis2 < min_dis*min_dis;

            int sum = 0;

            for (auto& value : ok_t)
            {
                sum = sum + nc::int32(value);
            }
            if (sum > best_score)
            {
                best_score = sum;
                best_arg = i;
                best_ok = ok_t;
            }
        }


        auto A1_new = nc::NdArray<double>(int(best_score), A1.shape().cols);
        auto A2_new = nc::NdArray<double>(int(best_score), A2.shape().cols);
        int j = 0;
        for (size_t i = 0; i < A1.shape().rows; i++)
        {
            if (best_ok.at(i) == 1)
            {
                A1_new.put(j, nc::Slice(A1.shape().cols), A1(i, A1.cSlice()));
                A2_new.put(j, nc::Slice(A2.shape().cols), A2(i, A2.cSlice()));
                j = j + 1;
            }
        }

        auto A_best = nc::vstack({ A1_new,A2_new });

        nc::NdArray<double> U;
        nc::NdArray<double> s;
        nc::NdArray<double> vt;
        nc::linalg::svd(A_best, U, s, vt);

        auto vt_T = nc::transpose(vt);
        auto h = vt_T(vt_T.rSlice(), 8);
        auto H = h.reshape(3, 3);
        nc::NdArray<double> a = { {1 / scale_value,0,0},{0,1 / scale_value,0},{0,0,1} };
        nc::NdArray<double> b = { {scale_value,0,0},{0, scale_value,0},{0,0,1} };
        auto item_1 = nc::dot(a, H);
        H = nc::dot(item_1, b);

        return H;
    }




}

void mosaic_global(Mat &im1, Mat &im2, nc::NdArray<double> &H, Mat &im1_p, Mat &im2_p)
{
    nc::NdArray<double> box2 = nc::NdArray<double>{ {0.0, double(im2.cols) - 1, double(im2.cols) - 1, 0},{0, 0,double(im2.rows) - 1, double(im2.rows) - 1},{1, 1, 1,1} };
    nc::NdArray<double> U;
    nc::NdArray<double> s;
    nc::NdArray<double> vt;
    auto box2_ = nc::random::randN<double>(nc::Shape(3, 4));
    for (size_t i = 0; i < 4; i++)
    {
        box2_.put(nc::Slice(3), i, nc::linalg::lstsq(H, box2(box2.rSlice(), i)));
    }

    cout << box2 << endl;
    cout << box2_ << endl;
    cout << box2_(0, box2_.cSlice()) / box2_(2, box2_.cSlice()) << endl;
    box2_.put(0, box2_.cSlice(), box2_(0, box2_.cSlice()) / box2_(2, box2_.cSlice()));
    box2_.put(1, box2_.cSlice(), box2_(1, box2_.cSlice()) / box2_(2, box2_.cSlice()));


    cout << box2_ << endl;

    auto u0 = nc::min(box2_(0, box2_.cSlice())).at(0);
    if (u0 > 0)
        u0 = 0;

    double u1 = 0;

    if ((im1.cols - 1) > nc::max(box2_(0, box2_.cSlice())).at(0))
    {
        u1 = im1.cols - 1;
    }
    else
    {
       u1 = nc::max(box2_(0, box2_.cSlice())).at(0);
    }
    auto ur = nc::arange(u0, u1 + 1);
    auto mosaisw = ur.size();
    cout << mosaisw << endl;

    auto v0 = nc::min(box2_(1, box2_.cSlice())).at(0);
    if (v0 > 0)
        v0 = 0;

    double v1 = 0;

    if ((im1.rows - 1) > nc::max(box2_(1, box2_.cSlice())).at(0))
    {
        v1 = im1.rows - 1;
    }
    else
    {
        v1 = nc::max(box2_(1, box2_.cSlice())).at(0);
    }
    auto vr = nc::arange(v0, v1 + 1);
    auto mosaish = vr.size();
    cout << mosaish << endl;

    cout << u0 << endl;
    im1_p = cv::Mat(cv::Size(mosaisw, mosaish), CV_32FC3);
    im2_p = cv::Mat(cv::Size(mosaisw, mosaish), CV_32FC3);
    int rows = im1.rows;
    int cols = im2.cols;

    cv::Mat xMapArray(im1_p.size(), CV_32FC1);
    cv::Mat yMapArray(im1_p.size(), CV_32FC1);

    for (int i = 0; i < mosaish; i++)
    {
        for (int j = 0; j < mosaisw; j++)
        {
            xMapArray.at<float>(i, j) = ur.at(j);
            yMapArray.at<float>(i, j) = vr.at(i);
        }
    }
    //进行变换
    cv::remap(im1, im1_p, xMapArray, yMapArray, cv::INTER_LINEAR, cv::BORDER_CONSTANT, cv::Scalar(0, 0, 0));

    auto z_ = H(2, 0)*xMapArray + H(2, 1)*yMapArray + H(2, 2);
    auto u_ = (H(0, 0)*xMapArray + H(0, 1)*yMapArray + H(0, 2)) / z_;
    auto v_ = (H(1, 0)*xMapArray + H(1, 1)*yMapArray + H(1, 2)) / z_;

    cv::remap(im2, im2_p, u_, v_, cv::INTER_LINEAR, cv::BORDER_CONSTANT, cv::Scalar(0, 0, 0));

}
nc::NdArray<double> comp_KR(Mat &im1, Mat &im2, nc::NdArray<double> & fp, nc::NdArray<double> & tp)
{
    auto H = HM_ransac(fp, tp, 500, 0.1);
    return H;
}

//Mat seamlessclone(Mat &im1_p, Mat &im2_p, Mat &im1_mask, Mat &im2_mask)

extern "C"
JNIEXPORT void JNICALL
Java_org_pytorch_helloworld_MainActivity_seamlessclone( JNIEnv *env, jobject thiz,
                                                        jlong jim1_p,jlong jim2_p,
                                                        jlong jim1_mask, jlong jim2_mask,
                                                        jlong jim_result)
{

    Mat& im1_p = *(Mat*)jim1_p;
    Mat& im2_p = *(Mat*)jim2_p;
    Mat& im1_mask = *(Mat*)jim1_mask;
    Mat& im2_mask = *(Mat*)jim2_mask;
    Mat& result = *(Mat*)jim_result;

//    cv::Mat mask(im1_mask.rows, im1_mask.cols, CV_8UC3, cv::Scalar(0, 0, 0));
    cv::Mat mask=im1_mask;



//    cv::cvtColor(im1_mask , im1_mask , cv::COLOR_RGBA2RGB);

    // Check if the image is created successfully.
    if (!mask.data)
    {
        std::cout << "Could not open or find the mask" << std::endl;
        exit(EXIT_FAILURE);
    }

    int left_top_x = 891;//130;
    int left_top_y =457;// 50;
    int width = 617;//90;
    int height = 914;//100;
    cv::Point p3(left_top_x, left_top_y), p4(left_top_x + width, left_top_y + height);
    cv::Scalar colorRectangle2(255, 255, 255);
    cv::rectangle(mask, p3, p4, colorRectangle2, -1);
    cv::Point center(left_top_x + int(width / 2), left_top_y + int(height / 2));
//    Mat result;
    cv::seamlessClone(im2_p, im1_p, mask, center, result, 1);

//    return (jlong)(&result);
}

//void registration(Mat &im1_small, Mat &im2_small, Mat &im1_p, Mat &im2_p)
extern "C"
JNIEXPORT void JNICALL
Java_org_pytorch_helloworld_MainActivity_registration( JNIEnv *env, jobject thiz,
                                                       jlong jim1_small, jlong jim2_small,
                                                       jlong jim1_p,jlong jim2_p)
{
    nc::NdArray<double> fp;
    nc::NdArray<double> tp;
    Mat& im1_small = *(Mat*)jim1_small;
    Mat& im2_small = *(Mat*)jim2_small;
    Mat& im1_p = *(Mat*)jim1_p;
    Mat& im2_p = *(Mat*)jim2_p;

    load_data(im1_small, im2_small, fp, tp);

    auto H = comp_KR(im1_small, im2_small, fp, tp);

//    im1_p = im1_small;
//    im2_p = im2_small;

    mosaic_global_v1(im1_small, im2_small, H, im1_p, im2_p);

}



extern "C"
JNIEXPORT void JNICALL
Java_org_pytorch_helloworld_ResultActivity_user_1mask_1seamlessclone(JNIEnv *env, jobject thiz,
                                                                     jlong im1_p_addr,
                                                                     jlong im2_p_addr,
                                                                     jlong des_addr, jint x, jint y,
                                                                     jint width, jint height) {
    Mat& im1_p = *(Mat*)im1_p_addr;
    Mat& im2_p = *(Mat*)im2_p_addr;

    cv::Mat mask(im1_p.rows, im1_p.cols, CV_8UC3, cv::Scalar(0, 0, 0));
    //mask = im1_mask;

    // Check if the image is created successfully.
    if (!mask.data)
    {
        std::cout << "Could not open or find the mask" << std::endl;
        exit(EXIT_FAILURE);
    }
    //int left_top_x = x;
    //int left_top_y = y;
    //int width = 50;
    //int height = 80;
    if (width / 2 != 0.5) width = width + 1;

    if (width / 2 != 0.5) height = height + 1;




    cv::Point p3(x, y), p4(x + width, y + height);
    cv::Scalar colorRectangle2(255, 255, 255);

    cv::rectangle(mask, p3, p4, colorRectangle2, -1);

    //cv::namedWindow("Display window", cv::WINDOW_AUTOSIZE);
    //cv::imshow("Display window", mask);

    //cv::waitKey(0);


    //imshow("raw1", im1_p);
    //imshow("raw2", im2_p);
    //waitKey(0);
    //destroyAllWindows();
    cv::Point center(x + int(width / 2), y + int(height / 2));
    Mat result;
    cv::seamlessClone(im2_p, im1_p, mask, center, result, 1);

    //return result
    Mat* des = (Mat*)des_addr;
    des->create(result.rows, result.cols, result.type());
    memcpy(des->data, result.data, result.rows * result.step);
}

void mosaic_global_v1(Mat &im1, Mat &im2, nc::NdArray<double> &H, Mat &im1_p, Mat &im2_p)
{
    // im1 拍摄的第一张图
    // im2 拍摄的第二张图
    // im1_p 是第一张warp后的结果图
    // im2_p 是第二张warp后的结果图
    // 该函数处理完毕后，im1是不变的
    // 该函数处理完毕后，im2改变了，它带出来的是im1 warp后的mask图
    nc::NdArray<double> box2 = nc::NdArray<double>{ {0.0, double(im2.cols) - 1, double(im2.cols) - 1, 0},{0, 0,double(im2.rows) - 1, double(im2.rows) - 1},{1, 1, 1,1} };
    nc::NdArray<double> U;
    nc::NdArray<double> s;
    nc::NdArray<double> vt;
    auto box2_ = nc::random::randN<double>(nc::Shape(3, 4));
    for (size_t i = 0; i < 4; i++)
    {
        box2_.put(nc::Slice(3), i, nc::linalg::lstsq(H, box2(box2.rSlice(), i)));
    }

    cout << box2 << endl;
    //cout << H << endl;
    //cv2.remap(src, map_x_32, map_y_32, cv2.INTER_LINEAR)
    cout << box2_ << endl;
    cout << box2_(0, box2_.cSlice()) / box2_(2, box2_.cSlice()) << endl;
    box2_.put(0, box2_.cSlice(), box2_(0, box2_.cSlice()) / box2_(2, box2_.cSlice()));
    box2_.put(1, box2_.cSlice(), box2_(1, box2_.cSlice()) / box2_(2, box2_.cSlice()));


    cout << box2_ << endl;

    auto u0 = nc::min(box2_(0, box2_.cSlice())).at(0);
    if (u0 > 0)
        u0 = 0;

    double u1 = 0;

    if ((im1.cols - 1) > nc::max(box2_(0, box2_.cSlice())).at(0))
    {
        u1 = im1.cols - 1;
    }
    else
    {
        //cout << nc::max(box2_(0, box2_.cSlice())) << endl;
        //cout << nc::max(box2_(0, box2_.cSlice())).at(0) << endl;
        u1 = nc::max(box2_(0, box2_.cSlice())).at(0);
    }
    auto ur = nc::arange(u0, u1 + 1);
    auto mosaisw = ur.size();
    cout << mosaisw << endl;

    auto v0 = nc::min(box2_(1, box2_.cSlice())).at(0);
    if (v0 > 0)
        v0 = 0;

    double v1 = 0;

    if ((im1.rows - 1) > nc::max(box2_(1, box2_.cSlice())).at(0))
    {
        v1 = im1.rows - 1;
    }
    else
    {
        v1 = nc::max(box2_(1, box2_.cSlice())).at(0);
    }
    auto vr = nc::arange(v0, v1 + 1);
    auto mosaish = vr.size();
    cout << mosaish << endl;

    cout << u0 << endl;
    im1_p = cv::Mat(cv::Size(mosaisw, mosaish), CV_32FC3);
    im2_p = cv::Mat(cv::Size(mosaisw, mosaish), CV_32FC3);


    Mat im1_mask(im1.rows, im1.cols, CV_8UC3, cv::Scalar(255, 255, 255));

    int rows = im1.rows;
    int cols = im2.cols;

    cv::Mat xMapArray(im1_p.size(), CV_32FC1);
    cv::Mat yMapArray(im1_p.size(), CV_32FC1);

    for (int i = 0; i < mosaish; i++)
    {
        for (int j = 0; j < mosaisw; j++)
        {
            xMapArray.at<float>(i, j) = ur.at(j);
            yMapArray.at<float>(i, j) = vr.at(i);
        }
    }
    //cout << xMapArray << endl;
    //cout << yMapArray << endl;

    //进行变换
    cv::remap(im1, im1_p, xMapArray, yMapArray, cv::INTER_LINEAR, cv::BORDER_CONSTANT, cv::Scalar(0, 0, 0));

    //cv::imshow("im1_p", im1_mask);
    //cv::imshow("im1", im2);
    //cv::waitKey(0);
    //cv::destroyAllWindows();

    auto z_ = H(2, 0)*xMapArray + H(2, 1)*yMapArray + H(2, 2);
    auto u_ = (H(0, 0)*xMapArray + H(0, 1)*yMapArray + H(0, 2)) / z_;
    auto v_ = (H(1, 0)*xMapArray + H(1, 1)*yMapArray + H(1, 2)) / z_;

    cv::remap(im2, im2_p, u_, v_, cv::INTER_LINEAR, cv::BORDER_CONSTANT, cv::Scalar(0, 0, 0));
//    cv::imshow("im2_p", im2_p);
//    cv::imshow("im2", im2);
//    cv::waitKey(0);
//    cv::destroyAllWindows();

    im2 = cv::Mat(cv::Size(mosaisw, mosaish), CV_32FC3);
    cv::remap(im1_mask, im2, xMapArray, yMapArray, cv::INTER_LINEAR, cv::BORDER_CONSTANT, cv::Scalar(0, 0, 0));
//    cv::imshow("im2", im2);
//    cv::waitKey(0);
//    cv::destroyAllWindows();



}


extern "C"
JNIEXPORT jintArray JNICALL
Java_org_pytorch_helloworld_ImageProcBaseActivity_Clip(JNIEnv *env, jobject thiz, jlong im2_small_addr,
                                                       jlong im1_p_addr, jlong im2_p_addr,
                                                       jlong im1_crop_addr, jlong im2_crop_addr  ) {

    Mat& im2_small = *(Mat*)im2_small_addr;
    Mat& im1_p = *(Mat*)im1_p_addr;
    Mat& im2_p = *(Mat*)im2_p_addr;
    Mat* im1_p_crop_j = (Mat*) im1_crop_addr;
    Mat* im2_p_crop_j = (Mat*) im2_crop_addr;

    // crop the im1 and im2 according to the inner max rect indicated by im2_small
    Mat b;
    extractChannel(im2_small, b, 0);
    vector<std::vector<cv::Point> > contours;
    vector<Vec4i> hierarchy;
    findContours(b,contours,hierarchy,RETR_TREE,CHAIN_APPROX_SIMPLE,  Point(0, 0));
    auto left_up = contours[0][0];
    auto right_down = contours[0][2];

    //auto im1_p_crop = im1_p(cv::Range(left_up.y, right_down.y + 1), cv::Range(left_up.x, right_down.x + 1));
    //auto im2_p_crop = im2_p(cv::Range(left_up.y, right_down.y + 1), cv::Range(left_up.x, right_down.x + 1));

    Mat im1_p_crop = im1_p(Rect(left_up, right_down));
    Mat im2_p_crop = im2_p(Rect(left_up, right_down));

    //copy the result
    im1_p_crop_j->create(im1_p_crop.rows, im1_p_crop.cols, im1_p_crop.type());
    memcpy(im1_p_crop_j->data, im1_p_crop.data, im1_p_crop.rows * im1_p_crop.step);

    im2_p_crop_j->create(im2_p_crop.rows, im2_p_crop.cols, im2_p_crop.type());
    memcpy(im2_p_crop_j->data, im2_p_crop.data, im2_p_crop.rows * im2_p_crop.step);


    jintArray mArray = env->NewIntArray(4);
    jint fill[4];
    fill[0] = left_up.y;
    fill[1] = right_down.y + 1;
    fill[2] = left_up.x;
    fill[3] = right_down.x + 1;

    env->SetIntArrayRegion(mArray, 0, 4,fill);
    return mArray;

}
extern "C"
JNIEXPORT void JNICALL
Java_org_pytorch_helloworld_ImageProcBaseActivity_exposure_1compensator(JNIEnv *env, jobject thiz,
                                                                        jlong im1_p_addr, jlong im2_p_addr) {
    // TODO: implement exposure_compensator()
    Mat& im1 = *(Mat*)im1_p_addr;
    Mat& im2 = *(Mat*)im2_p_addr;

    UMat p1, p2;
    im1.copyTo(p1);
    im2.copyTo(p2);
    cvtColor(p1, p1, COLOR_BGRA2BGR);
    cvtColor(p2, p2, COLOR_BGRA2BGR);
    vector<UMat> matVector;
    matVector.push_back(p1);
    matVector.push_back(p2);

    Point c1(0,0), c2(0,0);
    vector<Point> pointVector;
    pointVector.push_back(c1);
    pointVector.push_back(c2);

    UMat mask1(p1.rows, p1.cols, CV_8UC3, Scalar(255, 255,255));
    UMat mask2(p2.rows, p2.cols, CV_8UC3, Scalar(255, 255,255));
    vector<UMat> maskVector;
    maskVector.push_back(mask1);
    maskVector.push_back(mask2);

    Ptr<detail::ExposureCompensator> compensator = detail::ExposureCompensator::createDefault(detail::ExposureCompensator::GAIN_BLOCKS);
    compensator->feed(pointVector, matVector, maskVector);

    compensator->apply(0,c1,p1,mask1);
    compensator->apply(0,c2,p2,mask2);

    //convert umat to mat
    p1.copyTo(im1);
    p2.copyTo(im2);
}
extern "C"
JNIEXPORT void JNICALL
Java_helper_GraphCutSeamFinderHelper_GraphCutSeamFinder_1C(JNIEnv *env, jclass clazz, jlong img1_addr,
                                                        jlong img2_addr, jlong result_addr) {
    // TODO: implement GraphCutSeamFinder()
    Mat& canvas1 = *(Mat*)img1_addr;
    Mat& canvas2 = *(Mat*)img2_addr;
    Mat& result = *(Mat*)result_addr;

    //将两幅图剪切出来，剪切位置包含了配准（两幅图像的相对位置）信息
    Mat image1 = canvas1(Range::all(), Range(0, canvas1.cols / 2));
    Mat image2 = canvas2(Range::all(), Range(canvas2.cols / 4, canvas2.cols * 3 / 4));//假设大概1/2重复区域

    image1.convertTo(image1, CV_32FC3);
    image2.convertTo(image2, CV_32FC3);
    image1 /= 255.0;
    image2 /= 255.0;

    //在找拼缝的操作中，为了减少计算量，用image_small
    Mat image1_small;
    Mat image2_small;
    Size small_size1 = Size(image1.cols / 2, image1.rows / 2);
    Size small_size2 = Size(image2.cols / 2, image2.rows / 2);
    resize(image1, image1_small, small_size1);
    resize(image2, image2_small, small_size2);

    // 左图的左上角坐标
    cv::Point corner1;
    corner1.x = 0;
    corner1.y = 0;

    //右图的左上角坐标
    cv::Point corner2;
    corner2.x = image2_small.cols / 2;
    corner2.y = 0;

    std::vector<cv::Point> corners;

    corners.push_back(corner1);
    corners.push_back(corner2);

    std::vector<cv::UMat> masks;
    UMat imageMask1(small_size1, CV_8U);
    UMat imageMask2(small_size2, CV_8U);
    imageMask1 = Scalar::all(255);
    imageMask2 = Scalar::all(255);

    masks.push_back(imageMask1);
    masks.push_back(imageMask2);

    std::vector<cv::UMat> sources;

    UMat uimg1, uimg2;
    image1_small.copyTo(uimg1);
    image2_small.copyTo(uimg2);

    sources.push_back(uimg1);
    sources.push_back(uimg2);

    Ptr<cv::detail::SeamFinder> seam_finder = new cv::detail::GraphCutSeamFinder(cv::detail::GraphCutSeamFinderBase::COST_COLOR);
    seam_finder->find(sources, corners, masks);

    //将mask恢复放大
    resize(masks[0], imageMask1, image1.size());
    resize(masks[1], imageMask2, image2.size());

    Mat canvas(image1.rows, image1.cols * 3 / 2, CV_32FC3);
    image1.copyTo(canvas(Range::all(), Range(0, canvas.cols * 2 / 3)), imageMask1);
    image2.copyTo(canvas(Range::all(), Range(canvas.cols / 3, canvas.cols)), imageMask2);
    /*canvas *= 255;
    canvas.convertTo(canvas, CV_8UC3);*/

    //copy to result
    canvas.copyTo(result);

}