# 命令简介

`image` 读取图片信息

输出 JSON：`width`、`height`、`type`（图像类型码）、`typeName`（图像类型名）。
加 `-more` 时额外输出 EXIF 等元数据（`extra` 字段，按目录分组）。

# 用法

```bash
image [-more] 图片路径
  -more    # 同时输出 EXIF 等扩展元数据
```

# 示例

```bash
# 读取图片尺寸
image abc.jpg

# 含 EXIF 信息
image abc.jpg -more
```
