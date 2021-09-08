#ifndef LCL_MEASUREMENT_TOOL_IPERF_STATE_WRAPPER_INTERFACE_H
#define LCL_MEASUREMENT_TOOL_IPERF_STATE_WRAPPER_INTERFACE_H

#include <iperf_api.h>
#include <jni.h>

// TODO(matt9j) Should not be needed, pulling definition of iperf_test right now...
#include "iperf.h"

#ifdef __cplusplus
extern "C" {
#endif

/* ------------------------- */
/* -- Struct declarations -- */
/* ------------------------- */
struct iperf_test_state;
struct jni_callback;

/* ------------------------- */
/* - Function declarations - */
/* ------------------------- */

// Allocates a new iperf client and backing data structures.
// If a test state pointer is returned, delete_client_wrapper must be called.
struct iperf_test_state * new_client_wrapper();
int run_wrapper();
int stop_wrapper();
void delete_client_wrapper();

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

#ifdef __cplusplus
}
#endif

#endif //LCL_MEASUREMENT_TOOL_IPERF_STATE_WRAPPER_INTERFACE_H
