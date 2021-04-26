#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/wechat_qrcode.hpp>
#include <opencv2/imgcodecs.hpp>
#include <android/log.h>


#define CONSTRUCTOR(ENV, CLS) ENV->GetMethodID(CLS, "<init>", "(I)V")
#define ARRAYLIST(ENV) static_cast<jclass>(ENV->NewGlobalRef(ENV->FindClass("java/util/ArrayList")))
#define LIST_ADD(ENV, LIST) ENV->GetMethodID(LIST, "add", "(Ljava/lang/Object;)Z")

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "NativeLib", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "NativeLib", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "NativeLib", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "NativeLib", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "NativeLib", __VA_ARGS__)


using namespace std;
using namespace cv;


/// throw java exception
#undef throwJavaException
#define throwJavaException throwJavaException_wechat_qrcode

static void throwJavaException(JNIEnv *env, const std::exception *e, const char *method) {
    std::string what = "unknown exception";
    jclass je = 0;

    if (e) {
        std::string exception_type = "std::exception";

        if (dynamic_cast<const cv::Exception *>(e)) {
            exception_type = "cv::Exception";
            je = env->FindClass("org/opencv/core/CvException");
        }

        what = exception_type + ": " + e->what();
    }

    if (!je) je = env->FindClass("java/lang/Exception");
    env->ThrowNew(je, what.c_str());

    LOGE("%s caught %s", method, what.c_str());
    (void) method;        // avoid "unused" warning
}


void vector_Mat_to_Mat(std::vector<cv::Mat> &v_mat, cv::Mat &mat) {
    int count = (int) v_mat.size();
    mat.create(count, 1, CV_32SC2);
    for (int i = 0; i < count; i++) {
        long long addr = (long long) new Mat(v_mat[i]);
        mat.at<Vec<int, 2> >(i, 0) = Vec<int, 2>(addr >> 32, addr & 0xffffffff);
    }
}


jobject vector_string_to_List(JNIEnv *env, std::vector<std::string> &vs) {

    static jclass juArrayList = ARRAYLIST(env);
    static jmethodID m_create = CONSTRUCTOR(env, juArrayList);
    jmethodID m_add = LIST_ADD(env, juArrayList);

    jobject result = env->NewObject(juArrayList, m_create, vs.size());
    for (std::vector<std::string>::iterator it = vs.begin(); it != vs.end(); ++it) {
        jstring element = env->NewStringUTF((*it).c_str());
        env->CallBooleanMethod(result, m_add, element);
        env->DeleteLocalRef(element);
    }
    return result;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_cn_onlyloveyd_demo_contrib_wechat_WeChatQRCode_WeChatQRCode(JNIEnv *env, jobject thiz,
                                                                 jstring detector_prototxt_path,
                                                                 jstring detector_caffe_model_path,
                                                                 jstring super_resolution_prototxt_path,
                                                                 jstring super_resolution_caffe_model_path) {
    using namespace cv::wechat_qrcode;
    static const char method_name[] = "WeChatQRCode_WeChatQRCode";
    try {
        LOGD("%s", method_name);
        const char *utf_detector_prototxt_path = env->GetStringUTFChars(detector_prototxt_path, 0);
        std::string n_detector_prototxt_path(
                utf_detector_prototxt_path ? utf_detector_prototxt_path : "");
        env->ReleaseStringUTFChars(detector_prototxt_path, utf_detector_prototxt_path);
        const char *utf_detector_caffe_model_path = env->GetStringUTFChars(
                detector_caffe_model_path, 0);
        std::string n_detector_caffe_model_path(
                utf_detector_caffe_model_path ? utf_detector_caffe_model_path : "");
        env->ReleaseStringUTFChars(detector_caffe_model_path, utf_detector_caffe_model_path);
        const char *utf_super_resolution_prototxt_path = env->GetStringUTFChars(
                super_resolution_prototxt_path, 0);
        std::string n_super_resolution_prototxt_path(
                utf_super_resolution_prototxt_path ? utf_super_resolution_prototxt_path : "");
        env->ReleaseStringUTFChars(super_resolution_prototxt_path,
                                   utf_super_resolution_prototxt_path);
        const char *utf_super_resolution_caffe_model_path = env->GetStringUTFChars(
                super_resolution_caffe_model_path, 0);
        std::string n_super_resolution_caffe_model_path(
                utf_super_resolution_caffe_model_path ? utf_super_resolution_caffe_model_path : "");
        env->ReleaseStringUTFChars(super_resolution_caffe_model_path,
                                   utf_super_resolution_caffe_model_path);
        cv::wechat_qrcode::WeChatQRCode *_retval_ = new cv::wechat_qrcode::WeChatQRCode(
                n_detector_prototxt_path, n_detector_caffe_model_path,
                n_super_resolution_prototxt_path, n_super_resolution_caffe_model_path);
        return (jlong) _retval_;
    } catch (const std::exception &e) {
        throwJavaException(env, &e, method_name);
    } catch (...) {
        throwJavaException(env, 0, method_name);
    }
    return 0;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_cn_onlyloveyd_demo_contrib_wechat_WeChatQRCode_detectAndDecode(JNIEnv *env,
                                                                    jobject thiz,
                                                                    jlong self,
                                                                    jlong img_nativeObj,
                                                                    jlong points_mat_nativeObj) {
    using namespace cv::wechat_qrcode;
    static const char method_name[] = "WeChatQRCode_detectAndDecode";
    try {
        LOGD("%s", method_name);
        std::vector<Mat> points;
        Mat &points_mat = *((Mat *) points_mat_nativeObj);
        cv::wechat_qrcode::WeChatQRCode *me = (cv::wechat_qrcode::WeChatQRCode *) self;
        Mat &img = *((Mat *) img_nativeObj);
        std::vector<std::string> _ret_val_vector_ = me->detectAndDecode(img, points);
        vector_Mat_to_Mat(points, points_mat);
        jobject _retval_ = vector_string_to_List(env, _ret_val_vector_);
        return _retval_;
    } catch (const std::exception &e) {
        throwJavaException(env, &e, method_name);
    } catch (...) {
        throwJavaException(env, 0, method_name);
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_onlyloveyd_demo_contrib_wechat_WeChatQRCode_delete(JNIEnv *env, jobject thiz, jlong self) {
    delete (cv::wechat_qrcode::WeChatQRCode *) self;
}