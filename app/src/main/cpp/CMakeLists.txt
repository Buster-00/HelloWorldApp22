
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)


# Declares and names the project.

#include_directories(${CMAKE_SOURCE_DIR}/include)

project("HelloWorldApp")
#set(OpenCV_DIR D:/CameraXBasic/openCV/native)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

include_directories(${OpenCV_DIR}/jni/include)
include_directories(../include_numcpp)
include_directories(../boost_1_79_0)
add_library( lib_opencv SHARED IMPORTED )
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${OpenCV_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so)

add_library( # Sets the name of the library.
             HelloWorldApp


             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             cv.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

file(GLOB CVLIBS
        ${OpenCV_DIR}/staticlibs/${ANDROID_ABI}/libopencv_stitching.a)

target_link_libraries( # Specifies the target library.
        HelloWorldApp
        lib_opencv
        ${CVLIBS}
        # Links the target library to the log library
#        included in the NDK.
        ${log-lib} )
