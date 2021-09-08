#include <android/log.h>
#include <iperf_api.h>

#include "iperf3_jni_api.h"
#include "iperf3_java_callback.h"
#include "common_jni_util.h"

struct iperf_test_state test_state_wrapper;

JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_runIperfTest(
        JNIEnv *env, jobject iperfClient, jobject iperfConfig, jobject callback, jstring cacheDirTemplate) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering runIperfTest JNI function");

    test_state_wrapper.iperf_test = iperf_new_test();

    if (!test_state_wrapper.iperf_test) {
        __android_log_print(ANDROID_LOG_ERROR, __FILE_NAME__,"Unable to allocate test");
        return;
    }

    iperf_defaults(test_state_wrapper.iperf_test);    /* sets defaults */
    parse_java_config(env, &test_state_wrapper, iperfConfig, cacheDirTemplate);

    if (construct_java_callback(env, &test_state_wrapper, callback) < 0) {
        // TODO(matt9j) Need error handling here?
    }

    if (run_wrapper(&test_state_wrapper) < 0) {
        char *err_str = iperf_strerror(i_errno);
        // jni callback
        test_state_wrapper.jniCallback->on_error(&test_state_wrapper, err_str);
    }

    iperf_free_test(test_state_wrapper.iperf_test);
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting runIperfTest JNI function");
}

JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_stopIperfTest (JNIEnv * env, jobject iperfClient) {
    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Entering stopIperfTest JNI function");

    stop_wrapper(&test_state_wrapper);

    __android_log_print(ANDROID_LOG_VERBOSE, __FILE_NAME__, "Exiting stopIperfTest JNI function");
}
