命令简介
======= 

`aliyuniot pub` 向设备发布一条消息
    

用法
=======

```
aliyuniot pub [-msg $msg] [imeiA] [imeiB] ...
```

示例
======

```
->: aliyuniot -msg '{...}' 869300033624598
{
	869300033624598 : {
		success : true
	}
}
```