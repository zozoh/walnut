命令简介
======= 

`newsfeed readed` 修改一组消息的已读状态

用法
=======

```bash
newsfeed {FeedName} readed
  [ID1, ID2 ...]      # 消息 ID
  [-target 56..q1]    # 将 target 所有的消息设置已读/未读
  [-read true]        # true 标记为已读，false 标记为未读
  [-cqn]              # JSON 输出格式
```

示例
=======

```bash
# 标记一组消息已读
newsfeed readed -read true 45..6a 8y..w2

# 标记一组消息未读
newsfeed readed -read false 45..6a 8y..w2
```