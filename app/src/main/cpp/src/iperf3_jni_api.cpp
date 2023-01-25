#include <android/log.h>
#include <iperf_api.h>
#include <memory>

#include "iperf3_state_wrapper.h"
#include "iperf3_jni_api.h"
#include "iperf3_java_callback.h"
#include "common_jni_util.h"

extern "C" JNIEXPORT jint JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_runIperfTest(
        JNIEnv *env, jobject iperfClient, jobject iperfConfig, jobject callback, jstring cacheDirTemplate) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering runIperfTest JNI function");
    auto wrapper = std::make_unique<IperfStateWrapper>();

    wrapper->register_callbacks(env, iperfConfig, callback, cacheDirTemplate);
    if (wrapper->run_test() < 0) {
        // TODO(matt9j) Propagate actual error string.
        return -1;
    }

    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting runIperfTest JNI function");
    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_stopIperfTest (JNIEnv * env, jobject iperfClient) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering stopIperfTest JNI function");

    stop_wrapper();

    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting stopIperfTest JNI function");
}


extern "C" JNIEXPORT jint JNICALL Java_com_lcl_lclmeasurementtool_features_iperf_IperfClient_runIperfTest(
        JNIEnv *env, jobject iperfClient, jobject iperfConfig, jobject callback, jstring cacheDirTemplate) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering runIperfTest JNI function");
    auto wrapper = std::make_unique<IperfStateWrapper>();

    wrapper->register_callbacks(env, iperfConfig, callback, cacheDirTemplate);
    if (wrapper->run_test() < 0) {
        // TODO(matt9j) Propagate actual error string.
        return -1;
    }

    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting runIperfTest JNI function");
    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_features_iperf_IperfClient_stopIperfTest (JNIEnv * env, jobject iperfClient) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering stopIperfTest JNI function");

    stop_wrapper();

    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting stopIperfTest JNI function");
}
