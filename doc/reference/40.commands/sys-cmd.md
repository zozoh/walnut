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

- **`cron` / `schedule` / `task`** — 用途：定时任务调度（详见第 60 章） — 手册：✅
- **`mq`** — 用途：系统消息队列（send / secret） — 手册：✅
- **`lock`** — 用途：全局锁（try / get / free / list） — 手册：✅
- **`hookx`** — 用途：钩子调试与触发（get/query/invoke/view） — 手册：✅
- **`trash`** — 用途：回收站（mv / recover） — 手册：✅
- **`refer`** — 用途：对象引用计数（add/remove/view/count/objsha1） — 手册：✅
- **`mgadmin`** — 用途：MongoDB 直连管理（仅 root） — 手册：✅
- **`aclog`** — 用途：访问日志分析 — 手册：✅
- **`os`** — 用途：系统信息（字体列表等） — 手册：✅
- **`path`** — 用途：路径工具（JVM Handler） — 手册：❌
- **`datex`** — 用途：日期时间管道（8 个 @过滤器） — 手册：✅
- **`redis`** — 用途：Redis 直连操作（JVM Handler） — 手册：❌

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
