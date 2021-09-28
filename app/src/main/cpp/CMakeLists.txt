# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.16.3)

# Declares and names the project.
project("lcl_measurement_tool_native")
set (CMAKE_CXX_STANDARD 17)

# Include sub-projects by directory (must also be built with CMake).

# Gradle only allows one top-level native target, so if we add more native code we can include it as
# additional sub-projects here.
add_subdirectory(src)
add_subdirectory(libs)
