---
title: 命令机制
module: impl.box
keywords: [命令, 管道, 重定向, 沙箱, box, su, 身份切换, man]
source:
  - util/WnRun.java
  - impl/box/WnSystem.java
  - impl/box/JvmExecutorFactory.java
  - impl/box/cmd/man/zh_cn/
related: [everything-is-obj, js-scripting, env-vars, ../40.commands/README.md]
---

# 命令机制

在 Walnut 里，**一切操作都是命令**。命令可以拼装成管道，这是系统的主要「编程方式」。

## 执行模型

```bash
用户输入命令行
   │
   ▼
WnRun（命令行解析：管道 |、重定向 >、>>、变量 ${...}）
   │
   ▼
WnBoxService 分配沙箱（WnBox）── 每个沙箱绑定一个会话身份
   │
   ▼
JvmExecutorFactory 按命令名加载 cmd_* 类
   │
   ▼
cmd_*.exec(WnSystem sys, String[] args)
   WnSystem = {in, out, err, io, session, auth, me, envs, params}
```

**安全模型**：每条命令都在 `Wn.WC().su(user)` 的线程上下文中执行——命令能做什么，取决于它的**会话身份**，而不是谁发起了 HTTP 请求。HTTP API 之所以安全，就是因为 API 文件所有者的权限决定了命令的边界。

## 管道与重定向

```bash
# 管道：前一个命令的 stdout 是后一个的 stdin
cat ~/data.txt | grep ERROR | wc -l

# 重定向：写入对象（> 覆盖，>> 追加）
echo "hello" > ~/docs/a.txt
cat ~/docs/a.txt >> ~/docs/b.txt

# 模板渲染后批量执行（o -tmpl 生成命令文本，run 逐行执行）
o /domain -tmpl 'o id:@{id} -u \'tp:"A"\';' | run
```

## 会话与身份

```bash
whoami            # 当前身份
login superman    # 切到另一个账户（需 op 组管或 root 组成员），输出新会话
exit              # 退回父会话
```

- 多级登录构成会话树（`p_se_id` 链），见 [session](../30.auth/session.md)。
- 不登录也能执行：HTTP API 内部总是以 API 文件所有者的身份跑命令。

## 变量与环境

- `${xxx}`：命令行里的占位符，从环境变量和上下文取值（`echo $HOME` / `echo ${PATH}`）。
- 详细机制见 [env-vars](env-vars.md)。

## 帮助系统

```bash
man o             # 查看命令手册（中文）
man o -k size     # 手册内搜索关键词
cmds              # 列出全部可用命令
which ls          # 一个命令由谁实现（系统内置 or 用户 JS 覆盖）
```

- 内置命令手册：`impl/box/cmd/man/zh_cn/*.md`（约 76 篇）。
- 扩展命令手册：各 `ext/**/man/zh_cn/*.md`（约 300 篇子命令）。
- 域用户可在 `~/bin/` 放同名 JS/脚本**覆盖系统命令**，`which` 可辨别。

## 一个完整的实战脚本

```bash
# 每天把 ~/logs 下的错误日志归档并统计（配合 cron 使用）
mkdir -p ~/archive
cat ~/logs/app.log | grep ERROR > ~/archive/errors-$(date +%Y%m%d).log
echo "errors: $(cat ~/archive/errors-$(date +%Y%m%d).log | wc -l)"
```

## 注意事项

- 写句柄独占：管道中 `>` 打开的文件同时只能有一个写者。
- 命令超时：box 分配有超时（`box-alloc-timeout`，默认 5000ms），长任务应放到 [task/cron](../60.sched-async/cron-pipeline.md) 里跑。
- `run` 执行模板生成的命令时，每一行是一个独立沙箱。
