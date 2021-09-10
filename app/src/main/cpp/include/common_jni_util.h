#include <jni.h>

#ifndef INC_5GMPORTAL_CMII_JNI_UTIL_H
#define INC_5GMPORTAL_CMII_JNI_UTIL_H

#ifdef __cplusplus
extern "C" {
#endif

jstring charToJstring(JNIEnv* env, const char* str);

const char* getPackageName(JNIEnv *env);

char* getSafeTmpPath(JNIEnv *env);

const char* get_java_string_field(JNIEnv* env, jclass java_class, jobject object,
                                  const char *field, const char *signature);

#ifdef __cplusplus
}
#endif

#endif //INC_5GMPORTAL_CMII_JNI_UTIL_H
