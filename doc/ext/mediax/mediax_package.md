---
title  : MediaX包结构
author : zozoh
tags:
- 扩展
- mediax
---

```bash
#--------------------------------------------
# 接口实现
apis
  + AbstractMediaXAPI     # 抽象类：所有的API父类
  + NoTicketMediaXAPI     # 抽象类：所有不需要票据的API父类
  + ChinaZMeidaXAPI       # 实现类：ChinaZ
#--------------------------------------------
# 有用的 Bean
bean
  + MxAccount      # 对象: 账号
  + MxAType        # 枚举: 账号类型
  + MxCrawl        # 对象: 爬取的条件
  + MxPost         # 对象: 提交图文内容
  + MxReCrawl      # 对象: 爬取结果
  + MxRePost       # 对象: 提交结果
  + MxTicket       # 对象: 票据
#--------------------------------------------
# 子命令
hdl
  + mediax_crawl
  + mediax_download
  + mediax_post
#--------------------------------------------
# 服务实现类
impl
  + AbstractMediaXService  # 抽象服务
  + WnMeidaXService        # Walnut 的实现
#--------------------------------------------
# 帮助函数集
util
  + Mxs
#--------------------------------------------
MediaXAPI              # 操作接口
MediaXService          # 服务接口
cmd_mediax             # 命令
```