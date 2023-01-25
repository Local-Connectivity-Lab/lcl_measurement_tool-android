#include <jni.h>
/* Header for class com_cmii_iperf3_Iperf3Client */

#ifndef _INCLUDED_CMII_IPERF3
#define _INCLUDED_CMII_IPERF3

extern "C" {

JNIEXPORT jint JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_runIperfTest
        (JNIEnv *, jobject, jobject , jobject, jstring);

JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_Functionality_Iperf3Client_stopIperfTest
    (JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_lcl_lclmeasurementtool_features_iperf_IperfClient_runIperfTest
        (JNIEnv *, jobject, jobject , jobject, jstring);

JNIEXPORT void JNICALL Java_com_lcl_lclmeasurementtool_features_iperf_IperfClient_stopIperfTest
        (JNIEnv *, jobject);

}

#endif // _INCLUDED_CMII_IPERF3
