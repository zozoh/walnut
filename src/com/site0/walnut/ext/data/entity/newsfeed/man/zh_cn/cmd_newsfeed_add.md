命令简介
======= 

`newsfeed add` 添加一条消息

用法
=======

```bash
newsfeed {FeedName} add
  [Newsfeed Object]         # 消息对象
  [-target ID0,ID1..]       # 批量将给定的消息对象插入到目标
  [-cqn]                    # JSON 输出格式
```

Newsfeed Object
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
# 添加一条消息
newsfeed add '{content:"haha",type:"BROADCAST",sourceId:"e7..9a"}'

# 添加一条比较复杂的消息
cat demo_message.json | newsfeed add -cqn

# 批量插入一组消息
cat demo_message.json | newsfeed add -cqn -target e8..2q 89..5a u3..3z
```