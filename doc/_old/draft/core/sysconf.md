---
title:系统配置文件
author:zozoh
tags:
- 系统
---

# 系统配置文件的作用

一台 walnut 服务器，实际上是一个逻辑上的服务器。我们需要让服务器知道自己的某些信息。
譬如网络地址，监听端口号等

# 系统配置文件的格式

文件存放在 `/etc/sysconf` 路径下，是一个 JSON 文件

```
{
    // 系统的主域名以及监听端口
    mainHost : "zozoh.ngrok.wendal.cn",
    mainPort : 80
}
```

# 如何得到系统配置信息

- `cat /etc/sysconf` 如果你有权限的话
- `sys` 命令将一定能返回，因为它确保只读
- `Wn.getSysConf(io)` 函数可以在 Java 里返回配置项
- `WnSystem.getSysConf()` 函数可以在命令实现类提供便利方法
- `WnRun.getSysConf()` 函数为所有 `WnRun` 子类提供便利方法

