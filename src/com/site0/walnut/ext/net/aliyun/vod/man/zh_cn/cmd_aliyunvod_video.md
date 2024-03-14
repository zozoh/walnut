命令简介
======= 

`aliyunvod video` 用来获取一个或者多个视频的详细信息。
    

用法
=======

```bash
aliyunoss video  
  [VID1 [VID2 ...]]    # 视频ID，多个用空格隔开
  [-cqn]               # JSON 格式化
```

示例
=======

```bash
demo:~$ aliyunoss video 0063f3e137774e748e2d8ga506cq1ef5
{
   videoId: "0063f3e137774e748e2d8ga506cq1ef5",
   title: "TestVideo.mp4",
   status: "Normal",
   size: 991721024,
   duration: 2530.621,
   ...
}
```


