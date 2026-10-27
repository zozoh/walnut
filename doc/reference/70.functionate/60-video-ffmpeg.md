---
title: 视频信息与封图（videoi / videoc）
module: 70.functionate
keywords: [videoi, videoc, ffmpeg, 视频, 封图, 预览, 转码]
source: [ext/media/ffmpeg/]
related: [README, 50-image-imagic.md]
---

# 视频信息与封图（videoi / videoc）

视频上传后通常要两件事：读出时长宽高等信息、生成封面图和预览片段。
`videoi` 和 `videoc` 一对命令分别负责它们。

> 两个命令都依赖宿主机安装了外部程序 **ffmpeg**。

## videoi：读取视频信息

```bash
videoi ~/video/hi.avi
# 输出: {length:15.0, width:720, height:576, frameCount:0, frameRate:0}
```

输出 JSON，字段含义一目了然：时长（秒）、宽高、帧数、帧率。

## videoc：转码 + 生成封面与预览

```bash
videoc ~/video/hi.avi
```

转换后在原视频旁生成 `<视频名>.videoc` 目录：

```text
~/video/hi.videoc/
|-- _1_1.mp4        # 主视频（转码后的 mp4）
|-- _preview.jpg    # 预览图（取 38.2% 处的一帧）
|-- _preview.mp4    # 1/4 尺寸的预览片段
```

同时回写原视频对象的元数据：
`width` / `height` / `duration` / `video_cover` / `video_preview` / `videoc_dir`——
业务代码直接读对象元数据就能拿到封面和预览的地址。

常用参数：

```bash
videoc ~/video/hi.avi -mode '^preview'      # 只生成预览，不转主视频
videoc ~/video/hi.avi -bv 1500 -preset ultrafast   # 控制码率与转码速度
videoc ~/video/_raw.mp4 -F                  # `_` 开头的文件默认跳过，-F 强制
videoc ~/video/hi.avi -F -o                 # -o 输出完整元数据
```

## 典型场景：视频上传后处理

```bash
# 钩子或定时任务里：转码出预览，同时刷新元数据
videoc ${ph} -bv 1500
```

配合 IO 钩子（write 事件），视频落盘即自动转码，业务侧无感。
