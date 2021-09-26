#include <string.h>
#include <malloc.h>
#include "common_jni_util.h"

jstring charToJstring(JNIEnv* env, const char* str) {

    jclass clstring = (*env)->FindClass(env, "java/lang/String");
    //获取对象
    jmethodID mid = (*env)->GetMethodID(env, clstring, "<init>", "([BLjava/lang/String;)V");//获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(str));//建立jbyte数组
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(str), (jbyte*) str);//将char* 转换为byte数组
    jstring encoding = (*env)->NewStringUTF(env, "utf-8");// 设置String, 保存语言类型,用于byte数组转换至String时的参数
    return (jstring) (*env)->NewObject(env, clstring, mid, bytes, encoding);//将byte数组转换为java String,并输出
}

const char* get_java_string_field(JNIEnv* env, jclass class, jobject object,
        const char *field, const char *signature) {
    jfieldID server_field = (*env)->GetFieldID(env, class, field, signature);
    jstring server = (*env)->GetObjectField(env, object, server_field);
    return (*env)->GetStringUTFChars(env, server, NULL);
}
