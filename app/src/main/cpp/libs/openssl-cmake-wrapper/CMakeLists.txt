# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.16.3)

set(OPENSSL_DIRECTORY_NAME
    "openssl-1_1_1l"
    )

# Generate the expected filepaths based on the architecture.
if(${CMAKE_ANDROID_ARCH} MATCHES "^arm$")
    SET(OPENSSL_ARCH "openssl-armeabi-v7a")
elseif(${CMAKE_ANDROID_ARCH} MATCHES "^arm64$")
    SET(OPENSSL_ARCH "openssl-arm64-v8a")
elseif(${CMAKE_ANDROID_ARCH} MATCHES "^x86$")
    SET(OPENSSL_ARCH "openssl-x86")
elseif(${CMAKE_ANDROID_ARCH} MATCHES "^x86_64$")
    SET(OPENSSL_ARCH "openssl-x86_64")
else()
    message(FATAL_ERROR "No target string defined for arch: ${CMAKE_ANDROID_ARCH}")
endif()

add_library(crypto STATIC IMPORTED GLOBAL)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/${OPENSSL_DIRECTORY_NAME}/${OPENSSL_ARCH}/lib/libcrypto.a)

add_library(ssl STATIC IMPORTED GLOBAL)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/${OPENSSL_DIRECTORY_NAME}/${OPENSSL_ARCH}/lib/libssl.a)

add_library(openssl INTERFACE)
target_include_directories(openssl INTERFACE "${CMAKE_CURRENT_SOURCE_DIR}/${OPENSSL_DIRECTORY_NAME}/${OPENSSL_ARCH}/include")
target_link_libraries(openssl INTERFACE crypto ssl)
set_target_properties(openssl PROPERTIES INTERFACE_OPENSSL_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/${OPENSSL_DIRECTORY_NAME}/${OPENSSL_ARCH})
