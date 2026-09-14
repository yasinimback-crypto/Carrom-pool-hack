#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_carromassist_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string engineStatus = "Carrom Physics Engine Ready";
    return env->NewStringUTF(engineStatus.c_str());
}