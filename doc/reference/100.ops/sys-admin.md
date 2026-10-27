---
title: 系统管理与运维命令
module: 100.ops
keywords: [mgadmin, redis, refer, aclog, sysconf, domain list, os, 运维]
source: [ext/sys/, ext/data/domain/, util/Wn.java]
related: [../40.commands/03-cmd-filter.md, deploy, security-notes]
---

# 系统管理与运维命令概述

面向 root/运维的日常管理操作。命令索引详见 系统管理命令，本文是运维视角的用法汇编。

## 域名管理（/domain）

```bash
domain list                       # 列出所有域名映射
# 记录就是对象，直接操作：
echo '{}' > /domain/abc.com
o /domain/abc.com -e 'domain:abc; site:main; tp:B; expi_at:1790000000000'
o /domain/abc.com -del          # 下线
```

见 域名映射。

## Mongo 体检（mgadmin，root 专用）

```bash
mgadmin index        # 索引状况（缺索引是大慢查询的头号原因）
mgadmin raw_query '{"race":"cron_task","cron":{$regex:"T\\{"}}' --limit 5
mgadmin raw_count '...'
mgadmin raw_explain '...'      # 看执行计划是否走索引
mgadmin profile                # 性能监视器
mgadmin sanity_check           # 完整性检查
```

## Redis 直连（redis 命令）

无手册页，但 hdl 子命令齐全（见源码 `ext/sys/redis/hdl/`），相当于内置 Redis 客户端：

```bash
redis get mykey            # 字符串
redis hgetall myhash       # 哈希
redis keys 'cron*'         # 键扫描
redis ttl mykey            # 剩余有效期
redis info                 # 服务器信息
```

常规运维检查：

```bash
lock @list -G                # 看全局锁（调度锁是否残留）
```

锁残留（如节点崩溃后的分钟计划锁）会导致调度停摆，确认后用 `lock @free` 清理。

## 引用计数（refer）

清理存储前确认内容没被引用：

```bash
refer @view <SHA1>     # 谁在用这个内容
refer @count <SHA1>
```

## 访问日志（aclog）

```bash
aclog analysis          # 访问日志分析报表
```

web.xml 里还有可选的 `AccessLogFilter`（默认注释）输出原始访问日志。

## 系统配置（/etc/sysconf）

`Wn.getSysConf(io)` 读取 `/etc/sysconf`（JSON 对象，可含 MIME、全局参数等），默认不存在（代码里有 TODO 注释）。需要全局动态参数时可自建：

```bash
echo '{}' > /etc/sysconf
o /etc/sysconf -e '{...}'
```

## 其他运维小命令

```bash
os -fonts        # 服务器可用字体（验证码/头像渲染依赖）
path             # 路径工具（hdl，见源码）
datex @set now @fmt '%F %T'    # 服务器时间确认
```

## 日常巡检清单

```bash
domain list                          # 域名是否到期
cron list                            # 定期任务是否正常（配合 schedule list）
task list                            # 任务栈是否积压
lock @list -G                        # 有无残留锁
mgadmin index                        # 索引健康
disk                                 # 存储水位
```

# 延伸阅读

- 系统管理命令 — lock/hookx/trash/refer 细节
- 部署
- 故障排查
