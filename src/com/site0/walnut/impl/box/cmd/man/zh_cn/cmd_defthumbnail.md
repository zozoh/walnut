# 命令简介

`defthumbnail` 为某类对象生成系统默认缩略图

从内置资源读取 `/thumbnail/<类型>.png`，在 `/etc/thumbnail/<类型>/` 下生成
256/128/64/32/24/16 各尺寸的缩略图（通过 `chimg` 缩放）。

# 用法

```bash
defthumbnail -tp 类型 [-r]
  -tp    # 对象类型（必填），对应内置资源 <类型>.png
  -r     # 强制重新生成（默认已存在则跳过）
```

# 示例

```bash
# 为 pdf 类型生成默认缩略图
defthumbnail -tp pdf

# 重新生成
defthumbnail -tp pdf -r
```
