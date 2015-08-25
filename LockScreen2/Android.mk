LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := lewa-keyguard-ext
LOCAL_STATIC_JAVA_LIBRARIES += lewa-download-manager
LOCAL_STATIC_JAVA_LIBRARIES += com.lewa.themes \
                                android-support-v4 \
                                lewa-support-v7-appcompat

LOCAL_PACKAGE_NAME := LewaLockScreen2
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
#         vendor/lewa/apps/LewaSupportLib/actionbar_4.4/res \

LOCAL_RESOURCE_DIR = \
             $(LOCAL_PATH)/res \
             vendor/lewa/apps/LewaSupportLib/actionbar_4.4/res \

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages lewa.support.v7.appcompat

include $(BUILD_PACKAGE)

