# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include $(call all-subdir-makefiles)

LOCAL_C_INCLUDES += /Users/feng/Documents/android-ndk-r7b/sources/cxx-stl/stlport/stlport
APP_STL := stlport_static

LOCAL_MODULE    := imagefilter
LOCAL_SRC_FILES := curve.cpp imagefilter.cpp

LOCAL_LDLIBS    := -llog
LOCAL_CFLAGS += -D__DEBUG__ 

LOCAL_LDLIBS += /Users/feng/Documents/android-ndk-r7b/sources/cxx-stl/stlport/libs/armeabi/libstlport_static.a

include $(BUILD_SHARED_LIBRARY)
