# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.16.3)

set(IPERF_DIRECTORY_NAME
    iperf-3.10.1
    )

# Generate toolchain paths manually according to https://developer.android.com/ndk/guides/other_build_systems.
# This may need to be updated if the NDK changes
string(TOLOWER "${CMAKE_HOST_SYSTEM_NAME}-${CMAKE_HOST_SYSTEM_PROCESSOR}" AUTOTOOLS_EXT_BUILD_ARCH)
message(STATUS "Toolchain build architecture: ${AUTOTOOLS_EXT_BUILD_ARCH}")
SET(AUTOTOOLS_EXT_TOOLCHAIN "${CMAKE_ANDROID_NDK}/toolchains/llvm/prebuilt/${AUTOTOOLS_EXT_BUILD_ARCH}")
if(${CMAKE_ANDROID_ARCH} MATCHES "^arm$")
    SET(AUTOTOOLS_EXT_TARGET "armv7a-linux-androideabi")
elseif(${CMAKE_ANDROID_ARCH} MATCHES "^arm64$")
    SET(AUTOTOOLS_EXT_TARGET "aarch64-linux-android")
elseif(${CMAKE_ANDROID_ARCH} MATCHES "^x86$")
    SET(AUTOTOOLS_EXT_TARGET "i686-linux-android")
elseif(${CMAKE_ANDROID_ARCH} MATCHES "^x86_64$")
    SET(AUTOTOOLS_EXT_TARGET "x86_64-linux-android")
else()
    message(FATAL_ERROR "No target string defined for arch: ${CMAKE_ANDROID_ARCH}")
endif()

SET(AUTOTOOLS_EXT_ABI "24")

if(NOT ${CMAKE_C_COMPILER_TARGET} MATCHES "android.*${AUTOTOOLS_EXT_ABI}$")
    message(ERROR "The CMake ABI version does not match the version set by gradle")
    message(ERROR "Update the AUTOTOOLS_EXT_ABI version to match")
    message(FATAL_ERROR "ABI \"${AUTOTOOLS_EXT_ABI}\" does not match target ${CMAKE_C_COMPILER_TARGET}")
endif()

# Link libiperf statically but with position independent code support to be embedded in a higher level dynamic library
SET(AUTOMAKE_EXT_CXX_FLAGS "${CMAKE_CXX_FLAGS} -static -fPIC")
SET(AUTOMAKE_EXT_C_FLAGS "${CMAKE_C_FLAGS} -static -fPIC")

include(ExternalProject)
ExternalProject_Add(
        iperf_autotools
        SOURCE_DIR ${IPERF_DIRECTORY_NAME}
        CONFIGURE_COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/${IPERF_DIRECTORY_NAME}/configure --host ${AUTOTOOLS_EXT_TARGET} AR=${AUTOTOOLS_EXT_TOOLCHAIN}/bin/llvm-ar CC=${AUTOTOOLS_EXT_TOOLCHAIN}/bin/${AUTOTOOLS_EXT_TARGET}${AUTOTOOLS_EXT_ABI}-clang AS=${AUTOTOOLS_EXT_TOOLCHAIN}/bin/${AUTOTOOLS_EXT_TARGET}${AUTOTOOLS_EXT_ABI}-clang CXX=${AUTOTOOLS_EXT_TOOLCHAIN}/bin/${AUTOTOOLS_EXT_TARGET}${AUTOTOOLS_EXT_ABI}-clang++ LD=${AUTOTOOLS_EXT_TOOLCHAIN}/bin/ld RANLIB=${AUTOTOOLS_EXT_TOOLCHAIN}/bin/llvm-ranlib STRIP=${AUTOTOOLS_EXT_TOOLCHAIN}/bin/llvm-strip CFLAGS=${AUTOMAKE_EXT_C_FLAGS} CXXFLAGS=${AUTOMAKE_EXT_CXX_FLAGS} --without-openssl --prefix=${CMAKE_CURRENT_BINARY_DIR}/${IPERF_DIRECTORY_NAME}
        PREFIX ${IPERF_DIRECTORY_NAME}
        BUILD_COMMAND make
        BUILD_IN_SOURCE 1
        BUILD_BYPRODUCTS ${CMAKE_CURRENT_BINARY_DIR}/${IPERF_DIRECTORY_NAME}/lib/libiperf.a
)

# Create a CMake "imported" "interface" library representing the outputs from the autotools process.
add_library(iperf INTERFACE IMPORTED GLOBAL)

# Let CMake know these directories can be created since they are referred below-- they would have
# been eventually created by automake
file (MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/${IPERF_DIRECTORY_NAME}/lib")
file (MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/${IPERF_DIRECTORY_NAME}/include")

# Pass the headers and libraries created by automake to dependent targets for linking and/or inclusion.
target_include_directories(iperf INTERFACE ${CMAKE_CURRENT_BINARY_DIR}/${IPERF_DIRECTORY_NAME}/include)
target_link_libraries(iperf INTERFACE "${CMAKE_CURRENT_BINARY_DIR}/${IPERF_DIRECTORY_NAME}/lib/libiperf.a")

# Make sure the iperf interface library target triggers the autotools build.
add_dependencies(iperf iperf_autotools)

# Add extra includes based on the current structure of the wrapper codebase.
# This somewhat breaks the API encapsulation.
# TODO(matt9j) Should ultimately not be needed if the API interface were used cleanly
target_include_directories(iperf INTERFACE ${CMAKE_CURRENT_SOURCE_DIR}/${IPERF_DIRECTORY_NAME}/src)
target_include_directories(iperf INTERFACE ${CMAKE_CURRENT_BINARY_DIR}/${IPERF_DIRECTORY_NAME}/src)
