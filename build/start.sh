#!/bin/bash
############################################################
#
# Walnut4Java 启动脚本（Linux / 容器）
#
# 构建时由 build/wn4j-make.py 原样拷进产物的 sbin/start.sh，
# 镜像 Dockerfile 的 CMD 就是它。
#
#-----------------------------------------------------------
#
# # 可选环境变量（不设就用下面的缺省）
#
# $WALNUT_HOME   程序目录；不设则从本脚本所在目录往上找 sbin/start.sh
# $JAVA_HOME     JVM；缺省 /usr/lib/jvm/java-current/
# $WL_PID_PATH   运行锁；缺省 /var/run/walnut.pid
# $WN_MAIN_CLASS 启动类；缺省 com.site0.walnut.web2.WnLauncher2
#                （Jetty 12 重构后旧的 com.site0.walnut.web.WnLauncher 已删除）
#
############################################################
# 首先让脚本所在目录的父目录成为工作目录
if [ "$WALNUT_HOME" = "" ]; then
  WALNUT_HOME=`realpath "$(dirname "$0")"`
  echo "Find WALNUT_HOME from:" $WALNUT_HOME
  while [ ! -e "$WALNUT_HOME/sbin/start.sh" ]; do
    WALNUT_HOME=`realpath "$(dirname "$WALNUT_HOME")"`
    if [ "/" = "$WALNUT_HOME" ] || [ "" = "$WALNUT_HOME" ]; then
      break
    fi
  done
fi
if [ "$JAVA_HOME" = "" ]; then
  echo "Set default JAVA_HOME => /usr/lib/jvm/java-current/"
  export JAVA_HOME="/usr/lib/jvm/java-current/"
fi
if [ "$WL_PID_PATH" = "" ]; then
  echo "Set default WL_PID_PATH => /var/run/walnut.pid"
  export WL_PID_PATH="/var/run/walnut.pid"
fi
if [ "$WN_MAIN_CLASS" = "" ]; then
  echo "Set default WN_MAIN_CLASS => com.site0.walnut.web2.WnLauncher2"
  export WN_MAIN_CLASS="com.site0.walnut.web2.WnLauncher2"
fi
echo "-----------------------------------"
echo "JAVA_HOME     : $JAVA_HOME"
echo "JAVA_OPTS     : $JAVA_OPTS"
echo "WL_PID_PATH   : $WL_PID_PATH"
echo "WN_MAIN_CLASS : $WN_MAIN_CLASS"
echo "WALNUT_HOME   : $WALNUT_HOME"
echo "-----------------------------------"
# 准备 Java 启动参数
# ! 不能采用 java -jar 的方式，因为 -jar 会无视掉 jvm 的 classpath 参数
# ! 只能采用 MainClass 的方式启动
JV_OPS="$JAVA_OPTS";
JV_OPS="$JV_OPS -Djava.net.preferIPv4Stack=true";
JV_OPS="$JV_OPS -Dfile.encoding=UTF-8";
JV_OPS="$JV_OPS -Dsun.jnu.encoding=UTF-8";
# Walnut 的 Jar 包路径
WN_JAR=$WALNUT_HOME/core/walnut.jar
# 循环得到依赖库
export CLASSPATH=$WALNUT_HOME/conf/
export CLASSPATH=$CLASSPATH:$WALNUT_HOME/core/conf/
export CLASSPATH=$CLASSPATH:$WALNUT_HOME/core/walnut.jar
for file in $WALNUT_HOME/deps/*
do
  export CLASSPATH=$CLASSPATH:$file
done
#echo $CLASSPATH
# 启动进程
cd $WALNUT_HOME;
pwd;
CMD="java $JV_OPS $WN_MAIN_CLASS"
echo $CMD;
$CMD
