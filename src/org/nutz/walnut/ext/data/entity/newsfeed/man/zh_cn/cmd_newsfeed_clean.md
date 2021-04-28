命令简介
======= 

`newsfeed clean` 清除指定消息接收者所有已读的普通消息

用法
=======

```bash
newsfeed {FeedName} clean
  [-target 46..8a]    # 指定 target ID
```

示例
=======

```bash
# 清除指定 target 的消息
newsfeed clean -target 8y..w2
```