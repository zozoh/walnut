#!/bin/bash
############################################################
#
# Walnut4Java 停止脚本（Linux / 容器）
#
# 构建时由 build/wn4j-make.py 原样拷进产物的 sbin/stop.sh
#
#-----------------------------------------------------------
#
# # 可选环境变量（不设就用下面的缺省，与 start.sh 保持一致）
#
# $WL_PID_PATH   运行锁；缺省 /var/run/walnut.pid
#
############################################################
if [ "$WL_PID_PATH" = "" ]; then
  echo "Set default WL_PID_PATH => /var/run/walnut.pid"
  export WL_PID_PATH="/var/run/walnut.pid"
fi
if [ ! -e "$WL_PID_PATH" ]; then
  echo "!!! $WL_PID_PATH Not Found !!!"
  echo "!!! Fail to stop walnut"
  exit 1
fi
PID=`cat "$WL_PID_PATH"`
echo "kill walnut: pid=$PID"
kill "$PID"
