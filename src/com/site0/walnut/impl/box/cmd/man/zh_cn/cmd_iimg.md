# 命令简介

`iimg` 读取图片信息并生成缩略图、压缩图片

自动为图片写入 `width`/`height` 元数据；同 SHA1 的图片只计算一次，
后续直接复用现成结果（`-force` 强制重算）。缩略图保存在
`/home/<域>/.thumbnail/gen/` 下，通过图片的 `thumb` 元数据关联。

# 用法

```bash
iimg 图片 [-thumb 宽x高] [-thumbta 目标对象] [-mode zoom|clip] [-force] [-Q]
        [-clip 'x,y,w,h'] [-compress] [-dst 路径] [-quality 0.9]
        [-width 1920] [-height 1080] [-native] [-cqn]
  -thumb      # 生成缩略图，尺寸如 64x64
  -thumbta    # 缩略图关联的目标对象（默认图片自己）
  -mode       # 缩略图模式：zoom 缩放（默认）/ clip 剪裁
  -force      # 强制重新计算（默认复用同 SHA1 的现成结果）
  -clip       # 剪裁区域 'x,y,w,h'（可作为独立剪裁操作，或与 -mode clip 配合）
  -compress   # 压缩图片（影响 dst/quality/width/height）
  -dst        # 压缩输出路径（默认原地压缩）
  -quality    # 压缩质量（默认 0.9）
  -width      # 压缩后最大宽（优先于 -height）
  -height     # 压缩后最大高
  -native     # 用本机 /usr/bin/convert 压缩（ImageMagick，限 Linux）
  -Q          # 不输出（默认输出对象 JSON）
  -cqn        # JSON 格式开关
```

# 示例

```bash
# 生成 64x64 缩略图，并输出图片信息
iimg ~/myphoto.jpg -thumb 64x64

# 强制重算
iimg ~/myphoto.jpg -thumb 64x64 -force

# 剪裁模式缩略图
iimg ~/myphoto.jpg -thumb 100x100 -mode clip

# 原地压缩
iimg ~/myphoto.jpg -compress

# 指定压缩质量和输出目标
iimg ~/myphoto.jpg -compress -quality 0.8 -dst ~/myphoto_small.jpg

# 限制最大宽度
iimg ~/myphoto.jpg -compress -width 1920
```
