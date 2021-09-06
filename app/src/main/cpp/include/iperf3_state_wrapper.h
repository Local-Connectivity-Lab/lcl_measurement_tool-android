#ifndef LCL_MEASUREMENT_TOOL_IPERF3_STATE_WRAPPER_H
#define LCL_MEASUREMENT_TOOL_IPERF3_STATE_WRAPPER_H

#include <iperf_api.h>
#include <jni.h>

// TODO(matt9j) Should not be needed, pulling definition of iperf_test right now...
#include "iperf.h"

/* ------------------------- */
/* -- Struct declarations -- */
/* ------------------------- */
struct iperf_test_state;
struct jni_callback;

/* ------------------------- */
/* - Function declarations - */
/* ------------------------- */
int run_wrapper(struct iperf_test_state *test);

/* ------------------------- */
/* --- Struct definitions -- */
/* ------------------------- */
struct jni_callback {
    JNIEnv *env;
    jobject callbackObj;
    /* descriptor: (Ljava/lang/String;I)V */
    jmethodID connectingMethod;

    /* descriptor: (Ljava/lang/String;ILjava/lang/String;I)V */
    jmethodID connectedMethod;

    /* descriptor: (FFLjava/lang/String;Ljava/lang/String;)V */
    jmethodID intervalMethod;

    /* descriptor: (FFLjava/lang/String;Ljava/lang/String;)V */
    jmethodID resultMethod;

    /* descriptor: (Ljava/lang/String;I)V */
    jmethodID errorMethod;

    /* callback functions */
    void (*on_connecting)(struct iperf_test_state *, char *, int);
    void (*on_connected)(struct iperf_test_state *, char[], int, char[], int);
    void (*on_interval)(struct iperf_test_state *, float, float, char[], char[]);
    void (*on_result)(struct iperf_test_state *, float, float, char[], char[]);
    void (*on_error)(struct iperf_test_state *, char *);
};

struct iperf_test_state {
    struct iperf_test * iperf_test ;
    struct jni_callback * jniCallback;
};

#endif //LCL_MEASUREMENT_TOOL_IPERF3_STATE_WRAPPER_H
