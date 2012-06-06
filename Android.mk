TOP_LOCAL_PATH:= $(call my-dir)

# Build activity

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := SQLiteManager
LOCAL_MODULE_TAGS  := optional tests

# LOCAL_REQUIRED_MODULES := jni stuff

include $(BUILD_PACKAGE)

# ============================================================

# Also build all of the sub-targets under this one: the shared library.
# include $(call all-makefiles-under,$(LOCAL_PATH))
