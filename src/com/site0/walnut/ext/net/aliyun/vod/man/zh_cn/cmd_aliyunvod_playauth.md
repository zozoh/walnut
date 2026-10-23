# 过滤器简介

`@playauth` 获取视频的播放凭证，以便阿里云官方播放器使用。

# 用法

```bash
aliyunvod @playauth 视频ID [-cqn]
       # 视频ID    # 媒体 ID
  -cqn    # JSON 格式化开关
```

# 示例

```bash
aliyunvod @playauth 0063f3e137774e748e2d8ga506cq1ef5
# {
#    requestId: "AE3DBEDD-5FCF-4C91-9BE4-5B2213720487",
#    playAuth: "eyJTZWN1cml0eVRva2VuIjoiQ0FJUzN3SjF...",
#    videoMeta: {
#       coverURL: "http://outin-c8d84111...",
#       duration: 2530.621,
#       status: "Normal",
#       title: "TestVideo.mp4",
#       videoId: "0063f3e137774e748e2d8ga506cq1ef5"
#    }
# }
```
