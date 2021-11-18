#include <string.h>
#include <malloc.h>
#include "common_jni_util.h"

jstring charToJstring(JNIEnv* env, const char* str) {

    jclass clstring = (*env)->FindClass(env, "java/lang/String");
    jmethodID mid = (*env)->GetMethodID(env, clstring, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(str));
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(str), (jbyte*) str);
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");
    return (jstring) (*env)->NewObject(env, clstring, mid, bytes, encoding);
}

const char* get_java_string_field(JNIEnv* env, jclass class, jobject object,
        const char *field, const char *signature) {
    jfieldID server_field = (*env)->GetFieldID(env, class, field, signature);
    jstring server = (*env)->GetObjectField(env, object, server_field);
    return (*env)->GetStringUTFChars(env, server, NULL);
}
