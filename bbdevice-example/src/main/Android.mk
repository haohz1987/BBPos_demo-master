# This Android.mk is empty, and >> does not build subdirectories <<.
# Individual packages in experimental/ must be built directly with "mmm".

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := bbdevice ksoap2-android-assembly

LOCAL_MODULE_TAGS := eng

#LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_SRC_FILES := $(call all-java-files-under, ../../src)

LOCAL_PACKAGE_NAME := bbpos-demon
LOCAL_DEX_PREOPT := false
LOCAL_CERTIFICATE := shared
LOCAL_JNI_SHARED_LIBRARIES :=
LOCAL_MODULE_PATH := $(TARGET_OUT_APPS)/../preinstall
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)


include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := bbdevice:../../libs/bbdevice-android-3.3.0.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += ksoap2-android-assembly:../../libs/ksoap2-android-assembly-2.4-jar-with-dependencies.jar
include $(BUILD_MULTI_PREBUILT)


