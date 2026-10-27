---
title: 二维码（qrcode）
module: 70.functionate
keywords: [qrcode, 二维码, 生成, 解码, 中心图标]
source: [ext/media/qrcode/]
related: [README]
---

# 二维码（qrcode） 概述
生成二维码（默认）或解码（`-d`），输出是图片二进制，
重定向到文件即可。

## 生成

```bash
echo 'I feel good' | qrcode > ~/myqrcode.jpg

# 常用参数
qrcode 'https://demo.com/x' -size 256 -fmt png -margin 1 -c Q
```

- `-size` — 边长像素，默认 256
- `-fmt` — 图片格式 `png | jpg`，默认 jpg
- `-margin` — 白边宽度
- `-c` — 容错级别 `L | M | Q | H`（H 最高，适合带中心图标）

## 带中心图标

```bash
qrcode 'https://demo.com/x' -icon id:xxxxxx -icsz 0.25
```

`-icon` 指定中心图标的对象 ID（或路径），`-icsz` 是图标占边长的比例。

## 解码

```bash
qrcode -d ~/myqrcode.jpg
# 输出二维码里的文本
```

## 典型场景：登录二维码

业务里常见的「扫码登录」就是：后端生成一个含一次性 token 的二维码，
前端展示；手机扫码后带着 token 调登录接口。
用 `qrcode` 生成图片、`expi` 元数据控制 token 有效期即可拼出整个流程。
