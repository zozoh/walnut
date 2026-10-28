---
title: cmd_o 命令
module: 40.commands
keywords: [cmd_o, o 命令, 对象操作, 过滤器链, 上下文]
source: [ext/data/o/cmd_o.java, ext/data/o/man/zh_cn/cmd_o.md]
related: [11-cmd-o-query.md, 12-cmd-o-load.md, 13-cmd-o-update.md, 14-cmd-o-context.md, 15-cmd-o-agg.md, 16-cmd-o-output.md]
---

# cmd_o 命令概述

`o` 是操作 Walnut 标准对象的核心命令：增删改查、读取内容、
树形整理、聚合统计、格式化输出，全部在一条命令里完成。

在 Walnut 的约定里，「数据操作」第一反应就是 `o`——就像 Linux 里
文件操作的第一反应是 `ls`/`cat`/`rm` 的组合，而 `o` 把这组合并成了一个
过滤器链。

## 为什么是一个过滤器链

`o` 是过滤命令（写法见 03-cmd-filter.md）：整条命令由一连串 `@过滤器`
组成，每个过滤器吃「上下文」、加工后再放回「上下文」，链尾隐式输出。

```bash
o ~/photos @query '{tp:"jpg"}' @update 'scanned:true' @json -cqn
#       ①入口        ②查询过滤        ③逐个更新        ④输出
```

- ① 路径是最常用的入口：把路径对应的对象放进上下文
- 中间可以串任意多个过滤器，顺序即执行顺序
- 链尾不加输出过滤器时，默认按 JSON 输出上下文对象

## 命令级参数

```bash
o [options] [[@filter filter-args...]...]

[PATH1 PATH2 ...]   # 多个对象入口
[-cqn]              # JSON 的格式化方式（compact/quote/不忽略 null）
[-l]                # 输出时强制列表，否则只有一个列表项时输出对象
[-subkey children]  # 对象的哪个键用来存储 children，默认 children
                    # @json/@ajax/@tree 等子命令会用到
[-noexists null|ignore]  # 如何处理不存在的路径：
                         # null 标记 null / ignore 忽略 / 默认抛错
```

## 30 个过滤器与七篇专题

`o` 的全部过滤器在 `man o`（`cmd_o.md`）里有权威清单。
按任务它们可以归入七类，每类有一篇专题讲「这组过滤器解决什么任务、
怎么组合」：

- 查询 —— 11-cmd-o-query.md —— @query @get @fetch @mime
- 进一步读取 —— 12-cmd-o-load.md —— @path @tree @group @refer @read
  @ancestors @children @list @join_one @join_many
- 修改删除 —— 13-cmd-o-update.md —— @create @update @delete @inc
- 操作上下文 —— 14-cmd-o-context.md —— @var @filter @push @pop @put
  @remove @clear @enter @name @flat
- 聚集统计 —— 15-cmd-o-agg.md —— @agg @size @count
- 输出 —— 16-cmd-o-output.md —— @zip @json @ajax @tmpl @tab @quiet
  @value @summary

专题讲任务与组合，**每个过滤器的参数细节以 man 页为准**
（`man/zh_cn/cmd_o_*.md`，共 38 篇），专题不抄参数表。

## 典型的一天

```bash
# 早上：看看昨天上传了多少文件
o ~/inbox @count '{ct:"[today-1d,today)"}'

# 上午：把一批图片打上标签
o ~/photos @query '{tp:"jpg"}' @update 'album:"2026-summer"' @quiet

# 下午：导出对账单
o ~/orders @query '{ct:"[today,today+1d)"}' @json '^(id|nm|total)$' -cqn

# 晚上：打包备份
o ~/inbox @query @zip ~/backup/inbox-`datex @fmt yyyyMMdd`.zip -quiet
```

# 延伸阅读

- man o —— 全部过滤器的权威参数手册
- 03-cmd-filter.md —— 过滤命令的写法与工作机制
- 10.concepts/01-std-obj.md —— 对象的字段体系
