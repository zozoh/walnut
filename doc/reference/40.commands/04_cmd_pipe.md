---
title: 命令管道
module: impl.box
keywords: [命令, 管道, pipe, JvmAtomRunner, run, 变量替换, 批量执行]
source:
  - impl/box/JvmAtomRunner.java
  - impl/box/JvmAtom.java
  - impl/box/JvmCmd.java
  - util/Cmds.java
related: [command-pipeline, custom-command, o, sqlx]
---

# 命令管道

单个命令的能力有限，管道把多个命令组合起来，是 Walnut 主要的「编程方式」。
前一命令的标准输出，直接作为后一命令的标准输入，无需中间文件。

## 语法

```bash
# 基本形态：| 连接任意多个命令
cat ~/logs/app.log | grep ERROR | wc -l

# 一条命令行里可以用多个 |，每个命令是一个「原子」（Atom）
cmd1 | cmd2 | cmd3

# 多行命令：换行或 ; 分隔，依次执行
mkdir -p ~/archive
cat ~/logs/app.log | grep ERROR > ~/archive/errors.txt ; echo done
```

解析规则（`Cmds.splitCmdAtoms`）：

- `|` 是管道分隔符，引号内的 `|` 不拆分
- 每段第一个词是命令名，其余是参数
- 行尾注释 `#` 与 `//` 开头的行被忽略

## 执行模型

```bash
用户输入一行命令文本
   │
   ▼
JvmAtomRunner.__run
   1. splitCmdAtoms 按 | 拆成 N 个原子
   2. 每个原子一个 WnSystem（独立 in/out/err）
   3. 相邻原子之间建一条 SyncWnTunnel（内存队列）
      原子 i 的 sys.out ──写入──▶ 隧道 ──读取──▶ 原子 i+1 的 sys.in
   4. 每个原子跑在独立线程上，边生产边消费
```

要点：

- 管道是**流式**的：不必等前一个命令全部跑完，后一个命令已经开始消费
- 所有原子共享同一会话身份（`su(sys.getMe())`），权限上下文一致
- 单个原子直接在当前线程执行（无管道时），多个原子才开线程

## 变量的魔法

命令文本在拆管道**之前**还要经过两轮预处理：

**1. 环境变量替换**（`WnTmplX.exec`，每轮管道执行前都做）

```bash
echo $HOME                    # 输出当前用户主目录路径
echo ${PATH}                  # 输出 PATH 环境变量
export A=42 ; echo ${A}       # 42（export 设置会话环境变量）
```

**2. 反引号命令替换**（`__extend_substitution`）

```bash
# 反引号内的命令先执行，其输出替换到命令行里
echo "today is `date '+%F'`"

# 组合示例：根据管道内容动态生成命令
o ~/docs -tmpl 'o id:@{id} -u \'star:1\';' | run
```

反引号替换发生在最外层，因此可以嵌套管道与重定向。

## `run`：命令文本批量执行

`run` 把标准输入读到的文本，**逐行**作为命令执行，每行一个独立沙箱：

```bash
# o -tmpl 生成批量命令文本，run 逐行执行
o /domain -tmpl 'o id:@{id} -u \'tp:"A"\';' | run

# 手工构造命令列表
echo 'touch ~/a.txt
touch ~/b.txt' | run
```

这是 Walnut 里「数据驱动批量操作」的核心套路：
`o @query` 查出数据 → 模板渲染成命令文本 → `run` 执行。
注意每一行都是独立沙箱，彼此没有状态共享；上下文传递要用 `export` 环境变量。

## 实战示例

```bash
# 每天把 ~/logs 下的错误日志归档并统计（配合 cron 使用）
mkdir -p ~/archive
cat ~/logs/app.log | grep ERROR > ~/archive/errors-$(date '+%Y%m%d').log
echo "errors: `cat ~/archive/errors-$(date '+%Y%m%d').log | wc -l`"

# 查询一批对象，更新后再查回校验
o ~/docs @query 'tp:"A"' -l -json | o ~/docs @update @json -c
```

## 注意事项

- **写句柄独占**：管道中 `>` 打开的文件同时只能有一个写者，多原子并发写同一对象会出错
- **命令超时**：box 分配有超时（`box-alloc-timeout`，默认 5000ms），长任务应放到 [task / cron](../60.sched-async/cron-pipeline.md) 里跑
- **每行一个沙箱**：`run` 执行的每行命令是独立沙箱，无共享变量（环境变量除外）
- 重定向（`>` / `>>` / `2>&1`）的完整规则见 [命令输入输出](05_cmd_std_in_out.md)
