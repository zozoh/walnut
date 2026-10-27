---
title: 图片处理（imagic）
module: 70.functionate
keywords: [imagic, 图片, 缩略图, 缩放, 裁剪, 旋转]
source: [ext/media/imagic/]
related: [README, 50-hooks]
---

# 图片处理（imagic）

上传图片后生成缩略图、把大图压成封面、按坐标裁剪——
`imagic` 命令用一个过滤器链完成单张图片处理。

源文件可以是本地路径或 http(s) URL；输出用 `-out` 指定文件，
`-out ~self~` 表示原地覆盖。

## 基本用法

```bash
imagic <图片> -filter '<过滤器链>' [-qa 品质] [-out 输出]
```

过滤器用空格串联，按顺序执行。`-qa` 是输出品质（0~1，默认 0.8）。

## 内置过滤器

- `scale(0.5)` — 按比例缩放；`scale(0.7, 0.5)` 宽高各自的比例；
  也可传像素尺寸 `scale(200, 150)`
- `cover(1920,1080)` — 缩放并裁剪到正好填满目标尺寸（封面图场景）
- `contains(w,h)` — 缩放保证整个图放进目标框内（不变形，可能留白）
- `clip(x,y,w,h)` — 裁剪。坐标传 int 是像素，传 float（小于 1）是相对比例
- `rotate(0~360)` — 旋转
- `autoexif(false)` — 开关 EXIF 自动旋转（默认开，手机照片 upright 靠它）

## 示例

```bash
# 生成 200x200 封面缩略图
imagic ~/photo/a.jpg -filter 'cover(200,200)' -out ~/photo/a_200.jpg

# 先生成封面，再压品质
imagic abc.jpg -filter 'scale(0.5) cover(1920,1080)' -qa 0.6 -out xyz.jpg

# 裁剪左上角 120x180
imagic abc.jpg -filter 'clip(0,0,120,180)' -qa 0.8 -out xyz.jpg

# 从相对位置裁剪
imagic abc.jpg -filter 'clip(0.3,0.3,120,180)' -qa 0.8 -out xyz.jpg
```

> ⚠️ `cmd_imagic` 的 man 文档示例里出现的 `mirror` 过滤器
> 在源码中并未注册（当前内置过滤器就是上面 6 个），照抄示例会报错，
> 以本篇为准。

## 典型场景：上传自动出多档缩略图

系统自带的 `/sys/hook/write/create_thumbnail` 钩子就是 imagic 的典型应用：
图片写入后自动生成各档缩略图，详见 IO 钩子篇。
