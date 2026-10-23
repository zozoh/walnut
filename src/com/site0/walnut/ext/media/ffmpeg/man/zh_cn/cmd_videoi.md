# 命令简介

`videoi` 读取视频文件的信息（时长、宽高、帧数、帧率）。

# 用法

```bash
videoi 视频文件路径
```

# 示例

```bash
# 读取视频信息
videoi ~/video/hi.avi
# 输出
# {
#     "length" : 15.0,
#     "width" : 720,
#     "height" : 576,
#     "frameCount" : 0,
#     "frameRate" : 0
# }
```
