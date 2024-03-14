命令简介
======= 

`newsfeed started` 修改一组消息的加星状态

用法
=======

```bash
newsfeed {FeedName} started
  [ID1, ID2 ...]      # 消息 ID
  [-star true]        # true 标记为加星，false 标记为普通
  [-cqn]              # JSON 输出格式
```

示例
=======

```bash
# 标记一组消息加星
newsfeed started -read true 45..6a 8y..w2

# 标记一组消息未读
newsfeed started -read false 45..6a 8y..w2
```