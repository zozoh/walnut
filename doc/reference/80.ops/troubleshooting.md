---
title: 日志与故障排查
module: 80.ops
keywords: [日志, 排错, e.api, e.box, 句柄, 缓存, 钩子调试]
source: [conf/log4j2.xml, util/Wlog.java]
related: [deploy, sys-admin, security-notes]
---

# 日志与故障排查

## 日志体系

配置在 `conf/log4j2.xml`。代码里用 `Wlog` 分类输出（`src/com/site0/walnut/util/Wlog.java`）：

| Logger | 内容 |
|---|---|
| `MAIN` | 主流程/启动 |
| `IO` | 对象 I/O |
| `AUTH` | 登录/会话/权限 |
| `BOX` | 命令执行沙箱 |
| `CMD` | 具体命令 |
| `HOOK` | 钩子触发 |

排查时把对应 Logger 调到 debug/trace，例如 `BOX` 看命令实际执行了什么。

## 常用诊断命令

```bash
# 命令层面
man <cmd>            # 先查手册
cmds                 # 列出可用命令
sys                  # 系统信息
disk                 # 存储用量
env                  # 当前环境变量

# 对象层面
o <路径> -debug    # 看对象全部元数据（API 排障第一步）
o <路径> -json -full
find <路径> -match '{race:"FILE"}' -l

# 钩子排查
hookx @get <对象> @keys '改动的键' @view    # 这个对象会触发哪些钩子
hookx @get <对象> @invoke write             # 手动触发一次

# 调度排查
cron list / schedule list / task list       # 三表联动看
cron preview <任务id>                        # 任务到底什么时候跑
lock @list -G                                # 是否有残留锁

# API 排查
xapi req <api> <account> <req> -vars ...    # 第三方请求 dry-run
```

## 常见错误速查

- **`e.api.nofound`**：API 路径未命中（含 `_ANY`）：确认对象存在（服务端不校验声明元数据与可执行位）
- **`e.api.forbid`**：站点鉴权失败：票据无效/过期，或 `pvg-assert` 角色不够
- **`e.api.auth`**：`api-access-token` 校验失败
- **`e.box.auth.NoSession`**：无会话执行了需要登录的命令
- **`e.cmd.vcode.check_fail`**：动态密码校验失败
- **`e.hook.home.noDir`**：钩子主目录存在但不是目录（`~/.hook/<action>` 被文件占用）
- **`域名转发过期`**：/domain 记录 `expi_at` 到期
- **`e.cmd.thing.*`**：撞上了已弃用的 Thing 体系，迁移到 obj/WnQuery

## 典型症状

### 定时任务不跑

1. `cron list` 任务在不在、表达式对不对（`cron explain`）；
2. `schedule list` 今天有没有 load（`schedule load <today>` 手动补）；
3. `lock @list -G` 分钟计划锁残留（节点崩溃常见）→ 确认后 `lock @free`；
4. 消费线程是否活着：看 `BOX`/`MAIN` 日志。

### API 返回了旧数据

- 命中了 `cache-*` 缓存：检查缓存对象（`cache-test.path` 指到的路径），`o -del` 强制失效；
- 指纹不对：`cache-finger-key` 的摘要与参数不一致导致每次重算——确认 `cache-finger-key` 是否该设。

### 句柄泄漏 / 临时对象堆积

- `hdl-timeout`（web.properties，默认 3600 秒）控制句柄回收；
- API 临时对象靠 `expi-*` 清理线程回收，检查 `expi-obj-clean-interval`；
- `disk` 看水位，`o /var -match '{expi_at:{$gt:0,$lt:现在}}' -l` 找该清未清的。

### 第三方对接不通

1. `xapi req`（不发送）看最终请求 URL/参数/头；
2. `xapi ak` 验证密钥；
3. 手动 `httpc` 重放，`-h` 看响应头；
4. 密钥/票据类问题：`access_token` 缓存文件删除后 `-force` 重取。

## 性能排查

- 慢查询：`mgadmin raw_explain` 看是否走索引 → `mgadmin index` 确认索引存在；
- API 慢：先确认没每次重建缓存（cache-finger 抖动），再看命令内 `o -match` 是否带索引键；
- 大目录操作：分页（`-limit/-skip`）+ 异步化（task/mq）。

## 延伸阅读

- 部署 — 日志配置与运行参数
- 系统管理
