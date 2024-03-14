命令简介
======= 

`aliyuniot shadow` 获取或设置设备影子数据
    
对设备非实时配置,都是通过设备影子来完成的

设备影子分成 
- 设备上传的值reported
- 界面设置的值desired
- 元数据,记录上述两者的最后修改时间

用法
=======

```
aliyuniot shadow [imei] [-dry] [-u xxx:xxx,yyy:zzz]
```

支持模拟更新参数-dry,不实际更新

影子数据详解
======

```
{
   "state": {
      "reported": { // 设备上传的值
         "rssi": 29, // 信号强度
         "PowerSwitch": "on", // 继电器状态, on开, off关
         "OpMode": "cron", // 操作模式, cron定时模式, remote远程直接控制
         "Cron": "on:000000-190000,off:190001-190500,on:190501-235959" // 定时模式下的设置字符串
      },
      "desired": { // 用户设置的数据,会下发给设备
         "PowerSwitch": "off", // 当OpMode=remote时才有效
         "OpMode": "remote",
         "Cron": "on:000000-190000,off:190001-190500,on:190501-235959"
      }
   },
   "metadata": { // 阿里云提供了state键值对的最后设置时间
      "reported": {
         "NetLed": {
            "timestamp": 1532689251
         },
         "rssi": {
            "timestamp": 1532775906
         },
         "PowerSwitch": {
            "timestamp": 1532775906
         },
         "OpMode": {
            "timestamp": 1532689254
         },
         "Cron": {
            "timestamp": 1532689254
         }
      },
      "desired": {
         "PowerSwitch": {
            "timestamp": 1532786041
         },
         "OpMode": {
            "timestamp": 1532786041
         }
      }
   },
   "timestamp": 1532786041,
   "version": 136 // 版本号是自增的,设备上传一次或用户设置一次,就会变化
}
```

更新配置
=============

本命令带-u参数, 可以更新设备影子, 从实现上说, npower on/off是本命令的封装

u参数更新的是 desired , 仅desired部分

```
aliyuniot shadow 869300033624598 -u '{OpMode:"remote", PowerSwitch:"on"}'
```

注意, 这只是把设置下发到设备,如果设备不在线,那么下次联网时才能生效