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
#include "opencv2/photo/photo.hpp"


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

void load_data(Mat &im1, Mat &im2, nc::NdArray<double> & fp, nc::NdArray<double> & tp)
{

    Ptr<SIFT> detector = SIFT::create();

    // Detect features and compute descriptors
    Mat descriptors_object, descriptors_scene;
    vector<KeyPoint> keypoints_object;
    vector<KeyPoint> keypoints_scene;

    vector<DMatch> good_matches;
    detector->detectAndCompute(im1, noArray(), keypoints_object, descriptors_object);
    detector->detectAndCompute(im2, noArray(), keypoints_scene, descriptors_scene);

    // Match descriptors using FLANN
    FlannBasedMatcher matcher;
    vector< vector<DMatch> > matches;
    matcher.knnMatch(descriptors_object, descriptors_scene, matches, 2);

    if (matches.size() <= 4) {
        cout << "Error: too few matches were found" << endl;
    }
    else
    {
        for (int i = 0; i < (int)matches.size(); i++) //THIS LOOP IS SENSITIVE TO SEGFAULTS
        {
            if ((matches[i][0].distance < 0.7*(matches[i][1].distance)) && ((int)matches[i].size() <= 2 && (int)matches[i].size() > 0))
            {
                good_matches.push_back(matches[i][0]);
            }
        }
        if (good_matches.size() >= 10)
        {
            fp = nc::NdArray<double>(3, good_matches.size());
            tp = nc::NdArray<double>(3, good_matches.size());
            for (int j = 0; j < (int)good_matches.size(); j++)
            {
                fp(0, j) = keypoints_object[good_matches[j].queryIdx].pt.x;
                fp(1, j) = keypoints_object[good_matches[j].queryIdx].pt.y;
                fp(2, j) = 1;

                tp(0, j) = keypoints_scene[good_matches[j].trainIdx].pt.x;
                tp(1, j) = keypoints_scene[good_matches[j].trainIdx].pt.y;
                tp(2, j) = 1;
            }
        }
    }

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

    mosaic_global(im1_small, im2_small, H, im1_p, im2_p);


}
