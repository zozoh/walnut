# 命令简介

`videoc` 视频转换命令：调用 ffmpeg 把视频转成 mp4，并生成预览图、预览视频。

转换产物写入原视频旁 `<视频名>.videoc` 目录，同时更新原视频对象的元数据（宽高、时长、封面、预览等）。

# 用法

```bash
videoc 视频文件 [-vkFo] [-mode 正则] [-thumb 尺寸] [-bv N] [-ba N] [-vcodec 编码] [-preset 预设] [-fps N] [-crop 参数] [-cqn]
  -v        # 显示详细的转换日志
  -k        # 不删除临时文件
  -F        # 文件名以 `_` 开头时默认不转换，此参数强制转换
  -o        # 输出转换后视频对象的完整元数据；默认输出转换目录 ID
  -mode     # 转换模式（对 "preview_image|preview_video|main_video" 做正则匹配）
            # 如只生成预览：-mode "^preview"；只生成主视频：-mode "main"
  -thumb    # 预览图的缩略图尺寸，默认 64x64
  -bv       # 视频码率（kbps），默认 3000
  -ba       # 音频码率（kbps），默认 64
  -vcodec   # 视频编码，默认 libx264
  -preset   # 编码预设，默认 veryfast（ultrafast/superfast/.../veryslow）
  -fps      # 帧率，默认 24
  -crop     # 裁剪参数 w:h:x:y；缺省时若文件名形如 `3-1_640x1080~标题.mp4` 会自动解析
  -cqn      # JSON 输出的格式化开关
```

# 输出

```bash
hi.avi             # 原始视频
.hi.avi.videoc     # 转换后目录
    _1_1.mp4       # 转换后的主视频
    _preview.jpg   # 预览图（取 38.2% 处的帧）
    _preview.mp4   # 预览视频（1/4 大小、1/3 码率）
```

同时更新原视频对象的元数据：

```bash
{
    width  : 720,      # 视频宽
    height : 576,      # 视频高
    duration : 15.0,   # 时长（秒）
    video_frame_count : 0,
    video_frame_rate  : 0,
    video_cover   : "id:xxx",   # 预览图对象
    video_preview : "id:xxx",   # 预览视频对象
    videoc_dir    : "id:xxx"    # 转换目录
}
```

# 示例

```bash
# 完整转换（预览图 + 预览视频 + 主视频）
videoc ~/video/hi.avi

# 只生成预览
videoc ~/video/hi.avi -mode '^preview'

# 强制转换 _ 开头的文件，并输出完整元数据
videoc ~/video/_raw.mp4 -F -o

# 指定码率和预设
videoc ~/video/hi.avi -bv 1500 -preset ultrafast
```
