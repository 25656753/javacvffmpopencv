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
jclass  cl=env->FindClass("com/mass/javacvffmpopencv/Main3Activity");
jmethodID  methodif=env->GetMethodID(cl,"show","()V");
env->CallVoidMethod(instance,methodif);
/*    env->ThrowNew(env->FindClass("sun/jvm/hotspot/debugger/DebuggerException"), "eeee");*/



}