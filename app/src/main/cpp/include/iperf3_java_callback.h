//
// Created by 申勇 on 2020/11/20.
//

#include <jni.h>
#include "iperf_state_wrapper_interface.h"

#ifndef INC_5GMPORTAL_CMII_JAVA_CALLBACK_H
#define INC_5GMPORTAL_CMII_JAVA_CALLBACK_H

extern int construct_java_callback(JNIEnv *env, struct iperf_test_state *test, jobject callback);

extern int parse_java_config(JNIEnv *env, struct iperf_test_state *test, jobject config, jstring cacheDir);

#endif //INC_5GMPORTAL_CMII_JAVA_CALLBACK_H
