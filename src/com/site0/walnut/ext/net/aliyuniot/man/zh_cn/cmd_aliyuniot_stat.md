命令简介
======= 

`aliyuniot stat` 用于获取设备状态
    

用法
=======

```
aliyuniot stat [imeiA] [imeiB] ...
```

示例
======

```
->: aliyuniot stat 869300033624598
[{
   firmwareVersion: "NPowerV1_2.0.5_Luat_V0027_8955_SSL_FLOAT", //设备版本号
   gmtCreate: "2018-06-30 23:42:52", // 设备创建数据
   gmtActive: "2018-06-30 23:46:59", // 设备第一次激活的时间
   gmtOnline: "2018-07-27 18:50:39", // 设备的最后在线时间
   status: "OFFLINE", // 当前状态, OFFLINE离线, ONLINE在线, UNACTIVE未激活
   ipAddress: "111.206.162.6", // 来源ip,通常无意义
}]
```