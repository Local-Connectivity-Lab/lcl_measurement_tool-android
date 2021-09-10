add_library( # Sets the name of the library.
        lcl_measurement_tool_native
        # Use shared linking
        SHARED
        # Paths to all source files
        iperf3_java_callback.c
        common_jni_util.c
        # Using a mix of c and c++
        iperf3_state_wrapper.cpp
        iperf3_jni_api.cpp
        )

target_include_directories(lcl_measurement_tool_native PUBLIC ../include)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library(
    log-lib  # Sets the name of the path variable.
    log  # Specifies the name of the NDK library that you want CMake to locate.
    )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
        lcl_measurement_tool_native
        ${log-lib}
        android
        iperf
        )

# No tests available yet
#if(BUILD_TESTING)
#    add_subdirectory(tests)
#endif()
