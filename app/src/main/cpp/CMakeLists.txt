cmake_minimum_required(VERSION 3.22.1)
project("mekanik_router")

add_library(
    mekanik_router
    SHARED
    manhattan_router.cpp
    native_lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    mekanik_router
    ${log-lib}
)
