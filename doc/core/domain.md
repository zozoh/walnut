---
title:域名URL重写机制
author:zozoh
tags:
- 系统
- 扩展
- URL
---

# 任何域名的接入

```
/domain
    ${id}                  # 每个记录一个目录对象
        20151001.log       # 根据服务器时间，直接决定日志文件的名称
        20150921.log       # 每个访问记录都有过期时间
```

# 域名记录的元数据

```
dmn_grp  : "abc"           # 域名对应的组名
dmn_host : "www.abc.com"   # 域名的网址
dmn_expi : $ams            # 过期时间，绝对毫秒数
```

# 配置文件决定了转发方式

```
# hostmap 文件的内容

    ^(/a)(.+)$
```


