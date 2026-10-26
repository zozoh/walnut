---
title: 对象存储映射
module: core.indexer.vofs
keywords: [对象存储映射, vofs, 虚拟对象文件系统, s3, cos, oss, obs, kodo, xo, 两段式ID]
source:
  - core/mapping/indexer/VofsIndexerFactory.java
  - core/mapping/bm/VofsBMFactory.java
  - core/mapping/support/WnVofsOptions.java
  - core/indexer/vofs/
related: [10-io-mapping, xo-storage, 31-bm-vxds, 40-indexer-file]
---

# 对象存储映射

把云对象存储的桶（S3/COS/OSS/OBS/Kodo），挂成 Walnut 对象树里的一棵子树。
`vofs`（虚拟对象文件系统）也是**索引与内容合并映射**：桶本身就是目录树——
对象存进桶里的路径，就是它在 Walnut 里的路径。

## mnt 语法

```json5
{
    // ... 其他元数据
    "mnt": "vofs://s3:/home/demo#backup"
}
```

```bash
vofs://{s3|cos|oss|obs|kodo}:{域主目录}#{配置名}
```

- `{s3|cos|oss|obs|kodo}` — 对象存储类型（当前实现：s3、cos；其余已预留）
- `{域主目录}` — 配置所在域的主目录，如 `/home/demo`
- `{配置名}` — 对象存储连接配置，存放在 `{域主目录}/.io/{type}/{配置名}.json5`

json5 配置（secretId/secretKey/bucket/region/临时密钥等）由 `xo` 命令体系管理，
格式见 对象存储集成。

## 两段式 ID

vofs 子树的对象 ID 最能体现两段式的设计：

```bash
1j46qulejghahr2e6jhgtd17ie:dmlkZW8vcGF0aC90by5tcDQ=
└──────┬──────────────┘  └────────┬────────┘
  映射根对象（全局索引）        base64(对象在桶里的路径)
                                 └─ 解码后是 "video/path/to.mp4"
```

全局索引只需登记映射根一个对象；子树里百万对象，都只是桶里的键。
对使用者透明——`o`、`cat`、查询照常用。

## 与 vxds 的区别

同样是内容进对象存储，两者取向不同：

- **`vofs`**（本篇）：保留目录结构，元数据也在桶里——适合视频库、图床这类
  「对象本来就是文件」的场景
- **vxds**：内容按 SHA1 签名平铺存储，元数据仍留 Mongo——
  适合大量需要去重的小中文件

## 当前支持度

- **已实现** — `s3`（AWS）、`cos`（腾讯云）
- **语法已预留** — `oss`（阿里云）、`obs`（华为云）、`kodo`（七牛）：
  参数校验已接受这些类型，但客户端实现仍抛 `e.vofs.UnsupportMode`

## 扩展阅读

- 对象存储集成 — json5 连接配置、`xo` 命令操作桶
- 文件系统映射 — 同样的合并简写思路，介质是本地磁盘
- 桶管理器 - vxds — 对象存储的另一种用法（内容签名去重）
