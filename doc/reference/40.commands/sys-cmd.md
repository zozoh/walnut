---
title: 系统管理命令
module: 40.commands
keywords: [lock, hook, hookx, trash, refer, mgadmin, aclog, os, path, datex, mq, redis]
source: [ext/sys/]
related: [command-model, ../60.sched-async/cron-pipeline.md, ../60.sched-async/hooks.md, ../60.sched-async/mq.md, ../80.ops/sys-admin.md]
---

# 系统管理命令

系统层的管理命令：锁、钩子、回收站、引用计数、Mongo 管理、访问日志、日期处理等。源码在 `src/com/site0/walnut/ext/sys/`。

> 参数细节以 `man <命令>` 为准。

## 命令一览

| 命令 | 用途 | 手册 |
|---|---|---|
| `cron` / `schedule` / `task` | 定时任务调度（详见第 60 章） | ✅ |
| `mq` | 系统消息队列（send / secret） | ✅ |
| `lock` | 全局锁（try / get / free / list） | ✅ |
| `hookx` | 钩子调试与触发（get/query/invoke/view） | ✅ |
| `trash` | 回收站（mv / recover） | ✅ |
| `refer` | 对象引用计数（add/remove/view/count/objsha1） | ✅ |
| `mgadmin` | MongoDB 直连管理（仅 root） | ✅ |
| `aclog` | 访问日志分析 | ✅ |
| `os` | 系统信息（字体列表等） | ✅ |
| `path` | 路径工具（JVM Handler） | ❌ |
| `datex` | 日期时间管道（8 个 @过滤器） | ✅ |
| `redis` | Redis 直连操作（JVM Handler） | ❌ |

## lock：全局锁

脚本里做互斥/防重入用。默认「域模式」：锁名自动加域名前缀；`-G` 为全局模式：

```bash
lock @try myJob        # 尝试申请（拿不到立即失败）
lock @get myJob        # 申请（阻塞等待）
lock @free myJob       # 释放
lock @list             # 查看当前持有的锁
```

底层由 IoC 里的锁服务提供（单机内存锁或 Redis 分布式锁，见 [部署](../80.ops/deploy.md)）。

## hook / hookx：钩子

`hookx` 是钩子调试命令——给定对象，查看或模拟触发 write/create/delete/meta/mount/move 六类事件的钩子：

```bash
hookx @query ~/data 'race:"IMG"' @keys 'thumb' @view   # 查看写钩子
hookx @get ~/data/abc.txt @invoke create                 # 实际触发 create 钩子
```

钩子机制（定义方式、匹配条件、`~/.hook` 与 `/sys/hook`）详见 [钩子](../60.sched-async/hooks.md)。

## trash：回收站

需要 root 先初始化：

```bash
mkdir /trash
chmod +x /trash
chmod +r /trash

# 放入回收站（重名自动加 _1 后缀）
trash mv /home/wendal/abc

# 还原（原路径已存在则报错）
trash recover /trash/wendal/abc
```

## refer：引用计数

索引服务记录「谁引用了这个内容（SHA1）」，用于内容去重和依赖检查：

```bash
refer @view 54af..9a2e     # 查看某 SHA1 被哪些对象引用
refer @count 54af..9a2e     # 引用计数
refer @add ~/dir           # 为目录内容增加引用
refer @remove ~/dir        # 移除引用
```

## mgadmin：Mongo 直连（root 专用）

```bash
mgadmin raw_query '{"race":"cron_task"}'    # 原生查询
mgadmin raw_count '...'                      # 原生统计
mgadmin raw_explain '...'                    # 执行计划
mgadmin index                                # 索引管理
mgadmin profile                              # 性能监视器
mgadmin sanity_check                         # 完整性检查
```

## aclog / os / path / datex / redis

```bash
aclog analysis                  # 访问日志分析报表
os -fonts                       # 列出系统可用字体（渲染验证码/头像时用）
path                            # 路径工具（hdl 子命令，源码 ext/sys/path/hdl）
redis                           # Redis 直连操作（hdl 子命令，无手册）

# datex：日历管道
datex @set 2026-10-21 @fmt 'yyyy-MM-dd E'
datex @set now @day +3w @fmt '%F'          # 3 个“周”后的日期
datex @holidays ~/data/cn-holidays-2026 @day +1b @fmt '%F'   # 下一个工作日
```

datex 上下文永远是 Calendar，过滤器：`@day @fmt @holidays @month @set @time @timezone`（`man datex day` 等子页）。

## mq：消息队列入口

```bash
mq secret    # 为当前用户生成队列密钥（存 ~/.mq/secret）
mq send      # 推送一条消息
```

机制详见 [消息队列](../60.sched-async/mq.md)。

## 典型场景

- **防重复跑批**：任务开头 `lock @try`；
- **排查钩子误触发**：`hookx @query ... @view`；
- **误删恢复**：`/trash` + `trash recover`；
- **清理孤儿内容**：`refer @view` 确认无引用后删对象。

## 注意事项

- `mgadmin` 只给 root 组，它是绕过 Walnut 校验的裸通道；
- `lock` 的域模式意味着不同域同名锁互不影响，排查死锁时确认模式一致；
- `trash` 需要 `/trash` 目录存在且权限正确，否则报错。

## 延伸阅读

- [定时任务](../60.sched-async/cron-pipeline.md)
- [钩子](../60.sched-async/hooks.md)
- [系统运维](../80.ops/sys-admin.md)
