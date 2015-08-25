LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := lewa-keyguard-ext

LOCAL_PACKAGE_NAME := LewaLockScreen
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_PACKAGE)


include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := lewa-keyguard-ext

LOCAL_MODULE := lewa-lockscreen-static
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

include $(BUILD_STATIC_JAVA_LIBRARY)
