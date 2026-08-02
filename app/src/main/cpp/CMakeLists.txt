cmake_minimum_required(VERSION 3.22.1)
project("native-forwarder")

# Find required libraries
find_library(OPENSSL_LIB ssl)
find_library(CRYPTO_LIB crypto)
find_library(log-lib log)  # Android logging library

# Create the shared library
add_library(native-forwarder SHARED native-forwarder.cpp)

# Link libraries
target_link_libraries(native-forwarder ${OPENSSL_LIB} ${CRYPTO_LIB} ${log-lib})

# Compiler optimizations
target_compile_options(native-forwarder PRIVATE
    -Os -fvisibility=hidden -fdata-sections -ffunction-sections
    -Wl,--gc-sections -Wl,--strip-all)
