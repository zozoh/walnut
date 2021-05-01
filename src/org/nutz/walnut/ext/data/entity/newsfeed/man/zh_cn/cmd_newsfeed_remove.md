命令简介
======= 

`newsfeed remove` 移除一组消息

用法
=======

```bash
newsfeed {FeedName} remove
  [ID1, ID2 ...]      # 消息 ID
  [-cqn]              # JSON 输出格式
```

示例
=======

```bash
# 移除指定消息
newsfeed remove 45..6a 8y..w2
```