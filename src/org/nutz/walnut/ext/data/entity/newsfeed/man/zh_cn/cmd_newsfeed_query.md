命令简介
======= 

`newsfeed query` 提供复杂查询功能

用法
=======

```bash
newsfeed {FeedName} query
  [Query Object]            # 复杂查询条件对象
  [-pn 1]                   # 第几页（1 base 默认 1）
  [-pgsz 20]                # 每页数据，默认 20
  [-sort '{ct:1,ext0:-1}']  # 排序对象 1:asc, -1:desc
  [-cqn]                    # JSON 输出格式
```

Query Object
=======

```js
{
  id     : "8t..3r",    // 指定的 ID
  // 指定类型
  //  - BROADCAST: 广播:全部用户
  //  - MULTICAST: 组播:全组用户
  //  - UNICAST: 单播:某个用户
  type   : "UNICAST",
  //---------------------------------------
  readed : true, 
  stared : false,
  //---------------------------------------
  // 时间戳区间
  // 所有的时间戳查询是一个区间字符串
  //  [开始, 结束]
  // 其中时间点可以支持宏
  //  - %ms:now
  //  - %ms:now+3d
  //  - %ms:now-12h
  //  - %ms:now+5m
  //  - %ms:now+12s
  //  - %ms:2019-02-13T23:34:12
  // 譬如查找10分钟之内的消息  [%ms:now-10m, ]
  createTime : "[%ms:now-10m,]" // 消息创建时间
  readAt : "[%ms:now-10m,]"     // 用户标记已读的时间
  //---------------------------------------
  sourceId : "t6..8a",
  sourceType : "user",
  targetId : "t6..8a",
  targetType : "user",
  //---------------------------------------
  // 标题或者内容可以是部分内容，将会模糊查找
  title : "薯片",
  content : "很香",
  //---------------------------------------
  // 10个扩展字段
  ext0 : "xxx",
  ...
  ext9 : "xxx"
}
```

> @see `org.nutz.walnut.ext.entity.newsfeed.FeedQuery`


示例
=======

```bash
# 根据一个复杂条件查询
cat demo_message.json | newsfeed query

# 仅仅获取某个指定 ID
newsfeed query '{id:"r5a..78q"}' -cqn
```