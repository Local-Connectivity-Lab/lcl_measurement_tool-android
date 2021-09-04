# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.16.3)

set(IPERF_DIRECTORY_NAME
    iperf-3.1.3
    )

# Declares and names the project.

project("iperf-cmake-wrapper")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        iperf-cmake-wrapper

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${IPERF_DIRECTORY_NAME}/src/cjson.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_api.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_client_api.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_error.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_locale.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_sctp.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_server_api.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_tcp.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_udp.c
        ${IPERF_DIRECTORY_NAME}/src/iperf_util.c
        ${IPERF_DIRECTORY_NAME}/src/main.c
        ${IPERF_DIRECTORY_NAME}/src/net.c
#        ${IPERF_DIRECTORY_NAME}/src/t_timer.c
#        ${IPERF_DIRECTORY_NAME}/src/t_units.c
#        ${IPERF_DIRECTORY_NAME}/src/t_uuid.c
        ${IPERF_DIRECTORY_NAME}/src/tcp_info.c
        ${IPERF_DIRECTORY_NAME}/src/tcp_window_size.c
        ${IPERF_DIRECTORY_NAME}/src/timer.c
        ${IPERF_DIRECTORY_NAME}/src/units.c
        )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#target_link_libraries( # Specifies the target library.
#        lcl_measurement_tool_native
#
#        # Links the target library to the log library
#        # included in the NDK.
#        ${log-lib}
#        )

# Include directories to expose to dependees
target_include_directories(
        iperf-cmake-wrapper
        PUBLIC
        ${IPERF_DIRECTORY_NAME}/src
        )