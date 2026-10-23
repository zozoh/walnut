# 过滤器简介

`@video` 获取一个或者多个视频的详细信息。

# 用法

```bash
aliyunvod @video 视频ID... [-cqn]
       # 视频ID    # 视频 ID，多个用空格隔开
  -cqn    # JSON 格式化开关
```

# 示例

```bash
aliyunvod @video 0063f3e137774e748e2d8ga506cq1ef5 -cqn
# {
#    videoId: "0063f3e137774e748e2d8ga506cq1ef5",
#    title: "TestVideo.mp4",
#    status: "Normal",
#    size: 991721024,
#    duration: 2530.621,
#    ...
# }
```
