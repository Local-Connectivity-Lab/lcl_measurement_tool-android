//
// Created by 申勇 on 2020/11/20.
//

#include <jni.h>
#include "iperf3_state_wrapper.h"

#ifndef INC_5GMPORTAL_CMII_JAVA_CALLBACK_H
#define INC_5GMPORTAL_CMII_JAVA_CALLBACK_H

extern int construct_java_callback(JNIEnv *env, struct iperf_test_state *test, jobject callback);

extern int parse_java_config(JNIEnv *env, struct iperf_test_state *test, jobject config);

#endif //INC_5GMPORTAL_CMII_JAVA_CALLBACK_H
