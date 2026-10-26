---
title: 数据同步与迁移
module: 70.functionate
keywords: [dsync, truck, 同步, 归档, tree 索引, 迁移, 备份]
source: [ext/sys/dsync/, ext/sys/truck/]
related: [../20.io-storage/10-io-mapping.md, ../80.ops/sys-admin.md]
---

# 数据同步与迁移

跨目录、跨域、跨实例搬数据的工具：`dsync`（增量同步 + 归档包）与 `truck`（索引/桶间迁移）。

## dsync：目录同步

配置 `~/.dsync/<ConfigName>.json`（默认 `dsync.json`），核心字段 `dirs`（同步项数组）与 `restoreActions`（恢复后动作）：

```js
{
  dirs : [{
    key  : "p0",              // 同步项键，默认 p{序号}
    path : "~/site",          // 要同步的路径
    leafOnly       : false,   // 只同步文件（叶子）
    ignoreThingSet : false,   // 忽略 ThingSet
    ignoreTop      : true,    // 忽略最顶层文件夹
    ignoreHidden   : false    // 忽略隐藏文件
  }, {
    key  : "p1",
    path : "~/data"
  }],
  restoreActions : [          // 恢复时匹配执行（详见 man dsync restore）
    { test : { mime : "^image/" }, run : "imagic ${id} -filter 'cover(256,256)' -out ${ph_256}" }
  ]
}
```

> 无论配置如何，`.dsync` 目录自身都会被无视。

### 产物布局

```text
~/.dsync/
|-- head/        最新同步结果：{ConfigName}/p0.tree、p1.tree、meta/p0.json
|-- pkg/         归档包：{ConfigName}-{SHA1}.zip（SHA1=各 tree SHA1 的聚合）
|-- cache/       解包缓存：{ConfigName}/{SHA1}/...
|-- data/        内容去重池：按 SHA1 命名的文件
```

### 树索引 .tree

每次同步生成；文件内容是文本行（`F:`/`D:` 开头 + 相对路径 + 元数据），外加同名 `meta/*.json` 存全部核心元数据（键为内容 SHA1），顶层元数据 `dsync_t` 记录索引时间。

### 子命令（`@filter` 风格）

```bash
dsync @tree       # 构建/加载索引树
dsync @archive    # 按树打归档包（去重存 data/）
dsync @unpack     # 解包到 cache/
dsync @restore    # 按上下文恢复域文件（执行 restoreActions）
dsync @pkgname    # 输出当前上下文包名
dsync @as         # 输出上下文信息
dsync @ids        # 输出对象 ID 列表
```

典型流程：`@tree → @archive`（源端打包）→ 包传给目标端 → `@unpack → @restore`（目标端恢复）。

## truck：索引/桶迁移

```bash
truck @from ...    # 设置源索引管理器或桶管理器
truck @to ...      # 设置目标
truck @buf ...     # 迁移缓冲/选项
```

前提：目标目录已用 `mount` 命令映射好（见 挂载映射）。手册：`man truck` / `man truck from` / `man truck to` / `man truck buf`。

## bulk 备份体系

`doc/_old/markdown/core-l1/c1-bulk-backup-restore.md` 是一套**备份还原的设计文档**（还原级别 R/I/D/A × 操作符 rl/nw/tr/ig/cp），**当前源码中没有对应命令实现**，引用时注意区分设计稿与现状。

## 典型场景

- **域搬家**：`dsync @tree @archive` 打包 → 新环境 `@unpack @restore`；
- **上云冷备**：cron 定时 `@archive`，`aliyunoss upload` 传包（见 支付宝与阿里云）；
- **存储后端切换**：`truck @from @to` 从旧桶迁到新桶。

## 注意事项

- 内容级去重依赖 SHA1，恢复后若 ID 变化，`replaceDom` 类 restoreActions 可修正 HTML 内的 ID 引用；
- 大目录首次建 tree 较慢，建议低峰执行；
- 归档包含全部元数据，跨环境恢复注意域与账号差异。

## 延伸阅读

- 挂载映射
- I/O 模型 — SHA1/内容寻址
