LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, src)

LOCAL_MODULE := lewa-keyguard-ext
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_STATIC_JAVA_LIBRARY)
