LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := iperf3
LOCAL_SRC_FILES :=  iperf-3.1.3/src/cjson.c            \
					iperf-3.1.3/src/iperf_api.c        \
					iperf-3.1.3/src/iperf_client_api.c \
					iperf-3.1.3/src/iperf_error.c      \
					iperf-3.1.3/src/iperf_locale.c     \
					iperf-3.1.3/src/iperf_sctp.c       \
					iperf-3.1.3/src/iperf_server_api.c \
					iperf-3.1.3/src/iperf_tcp.c        \
					iperf-3.1.3/src/iperf_udp.c        \
					iperf-3.1.3/src/iperf_util.c       \
					iperf-3.1.3/src/main.c             \
					iperf-3.1.3/src/net.c              \
					iperf-3.1.3/src/tcp_info.c         \
					iperf-3.1.3/src/tcp_window_size.c  \
                    iperf-3.1.3/src/timer.c            \
                    iperf-3.1.3/src/units.c
LOCAL_CFLAGS += -pie -fPIE -fPIC -s
LOCAL_C_INCLUDES += $(LOCAL_PATH)/iperf-3.1.3/src
include $(BUILD_EXECUTABLE)