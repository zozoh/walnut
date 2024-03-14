命令简介
=======

`newsfeed` 提供了对于信息流类数据的基本操作支持。

> 关于消息更多详情，请参看 `core-l1/c1-general-data-entity.md`


用法
=======

```bash
# 子命令
newsfeed {FeedName} add      # 添加消息
newsfeed {FeedName} query    # 获取某指定条件的消息
newsfeed {FeedName} remove   # 删除消息
newsfeed {FeedName} readed   # 标记消息已读/未读
newsfeed {FeedName} stared   # 给消息加/减星
newsfeed {FeedName} clean    # 清除所有已读消息

# 为默认的 newsfeed 添加一条信息
cat demo_message.json | newsfeed add
```