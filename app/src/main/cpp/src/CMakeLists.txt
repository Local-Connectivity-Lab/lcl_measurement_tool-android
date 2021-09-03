add_library( # Sets the name of the library.
        lcl_measurement_tool_native

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        iperf3_jni_api.c
        iperf3_java_callback.c
        common_jni_util.c
        ../include/common_jni_util.h
        ../include/iperf3_java_callback.h
        ../include/iperf3_jni_api.h
        )

target_include_directories(lcl_measurement_tool_native PUBLIC ../include)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
        lcl_measurement_tool_native

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        android
        iperf-cmake-wrapper
        )