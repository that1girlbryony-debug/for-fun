cmake_minimum_required(VERSION 3.22.1)
project("native-forwarder")

find_library(OPENSSL_LIB ssl)
find_library(CRYPTO_LIB crypto)

add_library(native-forwarder SHARED native-forwarder.cpp)

target_link_libraries(native-forwarder ${OPENSSL_LIB} ${CRYPTO_LIB} ${log-lib})

target_compile_options(native-forwarder PRIVATE
    -Os -fvisibility=hidden -fdata-sections -ffunction-sections
    -Wl,--gc-sections -Wl,--strip-all)