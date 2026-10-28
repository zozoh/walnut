---
title: cmd_o 进一步读取
module: 40.commands
keywords: [cmd_o, @read, @children, @tree, @refer, @list, @ancestors, 树形, 关联对象]
source: [ext/data/o/]
related: [10-cmd-o-overview.md, 14-cmd-o-context.md]
---

# cmd_o 进一步读取概述

查询拿到对象只是第一步。本篇讲「把对象读全」的一组过滤器：
读内容、读子节点、读成树、读关联对象、读祖先链。

## @read：读内容

`@read` 把对象的内容（字节流）读进元数据的某个键：

```bash
# 读对象本身内容到 content 键
o ~/cfg/app.json @read -as json @json '^(id|content)$' -cqn

# 读候选相对路径（常用于 API 模板做多语言兜底）
o ~/page/index @read .data/zh_cn.md .data/en_us.md -to content
```

- 候选路径依次尝试，对象是目录或路径不存在就试下一个；
  `~self~` 表示读自己，不给路径时默认就是它
- `~ignore~` 占位跳过——给 API 命令模板预留位置用的
- `-to` 目标键（默认 content），`-as json` 时内容解析成 JSON 对象

## @children / @list：读子节点

```bash
# 递归读两层，子节点放进 children 键
o ~/mydir @children -depth 2 -sort '{nm:1}'

# 按条件决定哪些目录继续展开，子节点再按条件过滤
o ~/mydir @children 'tp:"MyCate"' -query '{hidden:{$ne:true}}'

# 限量与层数
o ~/mydir @children -depth 3 -limit 500

# 读多个指定子节点，分别放到 pics/audios 键
o ~/site @list pics audios videos/index
```

`@children` 默认上限 1000（`-limit`），深度不限（`-depth <= 0`）——
读大树时记得控制这两个参数。

## @tree / @enter / @flat：树形整理

```bash
# 把目录向下展开 2 层并归纳成树
o ~/mydir @tree -depth 2

# 把一组散装对象归纳为一棵树（自动找公共根）
o f1 f2 f3 @tree

# 展开后，进入树中第一个匹配的对象作为新上下文
o ~/mydir @tree -depth 2 @enter 'nm:"abc"'

# 树展平回列表（只要叶子）
o ~/mydir @children -depth 2 -leaf @flat
```

> ⚠️ `@tree` 的 `-top` / `-root`、`-enter` 的逐层 Match、
> `@flat` 的 `-tagby` 打标签等参数见 man，这里只给主链路。

## @ancestors：祖先链

```bash
# 列出对象的祖先（到 ~ 为止，含自己）
o @fetch ~/abc/x/y/z.txt @ancestors -self

# 截止在某个祖先，且不把该祖先算进来
o @fetch ~/abc/x/y/z.txt @ancestors -until ~/abc
```

## @refer：关联对象详情

对象元数据里常有引用其他对象的 ID（如图片对象的 `thumb`）。
`@refer` 把这些引用展开成对象详情：

```bash
# 把 thumb 引用展开为 thumb_obj 键
o ~/photos @query @refer thumb -keys '^(id|nm|sha1|len|mime|width|height)$'

# 多个键；video_cover 键存的是 ID（=id 声明）
o ~/videos @refer 'thumb,video_cover=id'

# 针对 children 里的子对象展开（-get 指向子对象列表）
o ~/site @children @refer logo -get 'children' -keys '%SHA1'
```

- 展开结果默认放到 `${key}_obj` 键，可用第二个参数改名
- `-keys` 挑选展开对象的哪些键，`%SHA1` 等是快捷键
- 典型的「列表页带缩略图」场景：查询 + @refer 一次搞定

## @join_one / @join_many：关联查询

与 `@refer`（按 ID 展开）不同，`@join_one` / `@join_many`
是按路径模板 / 查询条件把**其他对象主动 join 进来**：

```bash
# 给每个订单 join 上它的用户（一对一）
o ~/orders @query @join_one -query '{id:"=uid"}' -to user_obj -fetch

# 给每个分类 join 它的商品列表（一对多）
o ~/categories @query @join_many -query -to items -limit 50
```

> ⚠️ 这两个过滤器的 man 页（cmd_o_join_one/join_many.md）
> 示例部分为空，参数以源码 `cmd_o.java` 的对应处理器为准；
> 另外 man 总表里的 `@group` 目前没有单独的 man 页。

# 延伸阅读

- man cmd_o_read / cmd_o_children / cmd_o_tree / cmd_o_refer —— 参数权威
- 14-cmd-o-context.md —— @enter / @flat 依赖的上下文机制
