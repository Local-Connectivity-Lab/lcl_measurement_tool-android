#include <android/log.h>
#include <iperf_api.h>

#include "iperf3_jni_api.h"
#include "iperf3_java_callback.h"
#include "common_jni_util.h"


JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_exec(
        JNIEnv *env, jobject iperfClient, jobject iperfConfig, jobject callback) {
    __android_log_print(ANDROID_LOG_VERBOSE, "lcl_meas", "Running client exec JNI function");

    //TODO(matt9j) Allocated on the stack for now... is this correct?
    struct iperf_test_state test_state_wrapper;
    struct iperf_test *test;

    test_state_wrapper.iperf_test = iperf_new_test();
    test = test_state_wrapper.iperf_test;

    if (!test)
        iperf_errexit(NULL, "create new test error - %s", iperf_strerror(i_errno));

    iperf_defaults(test);    /* sets defaults */
    parse_java_config(env, &test_state_wrapper, iperfConfig);

    if (construct_java_callback(env, &test_state_wrapper, callback) < 0) {
        // TODO(matt9j) Need error handling here?
    }

    if (run_wrapper(&test_state_wrapper) < 0) {
        char *err_str = iperf_strerror(i_errno);
        // jni callback
        test_state_wrapper.jniCallback->on_error(&test_state_wrapper, err_str);
        iperf_errexit(test, "error - %s", err_str);
    }

    iperf_free_test(test);
}

JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_stop (JNIEnv * env, jobject iperfClient) {
    __android_log_print(ANDROID_LOG_VERBOSE, "lcl_meas", "Running client stop JNI function");

    // TODO(matt9j) Need to gracefully shutdown the test
//    if (!test_holder) {
//        iperf_errexit(NULL, "create new test error - %s", iperf_strerror(i_errno));
//    }
//    iperf_got_sigend(test_holder);
//    iperf_free_test(test_holder);
}
