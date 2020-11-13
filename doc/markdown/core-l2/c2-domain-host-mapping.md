---
title: 域名映射机制
author: zozohtnt@gmail.com
tags:
- 概念
---

# 记录元数据

```bash
nm : "www.demo.com"      # 域名
tp : "A"                 # 记录类型，A: 网站, B: 仅获取域映射
                         # 不可识别的记录类型或者是空类型，均当作 "A"
expi   : AMS             # 过期时间
domain : "demo"          # 映射到的目标域
site   : "~/www/user"    # 直接指定站点目录
```