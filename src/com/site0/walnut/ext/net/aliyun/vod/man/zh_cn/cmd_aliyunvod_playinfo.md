# 过滤器简介

`@playinfo` 获取视频的播放信息，以便第三方播放器使用。

# 用法

```bash
aliyunvod @playinfo 视频ID [-cqn]
       # 视频ID    # 媒体 ID
  -cqn    # JSON 格式化开关
```

# 示例

```bash
aliyunvod @playinfo 0063f3e137774e748e2d8ga506cq1ef5
# {
#    requestId: "745CA159-BE5F-42D9-8439-F233B111F763",
#    playInfoList: [{
#       width: 640,
#       height: 360,
#       size: 172473530,
#       ...
#    }],
#    videoBase: {
#       outputType: "oss",
#       ...
#    }
# }
```
