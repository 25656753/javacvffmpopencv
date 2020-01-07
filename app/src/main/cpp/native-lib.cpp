#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_mass_javacvffmpopencv_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mass_javacvffmpopencv_Main3Activity_stringFromJNI(JNIEnv *env, jobject instance,
                                                           jclass t) {

jmethodID  methodif=env->GetMethodID(t,"show","()V");
env->CallVoidMethod(instance,methodif);
}