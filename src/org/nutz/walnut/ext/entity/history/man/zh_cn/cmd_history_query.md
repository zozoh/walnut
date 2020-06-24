命令简介
======= 

`history query` 提供复杂查询功能

用法
=======

```bash
history {HistoryName} query
  [Query Object]            # 复杂查询条件对象
  [-pn 1]                   # 第几页（1 base 默认 1）
  [-pgsz 20]                # 每页数据，默认 20
  [-sort '{ct:1,uid:-1}']   # 排序对象 1:asc, -1:desc
  [-cqn]                    # JSON 输出格式
```

Query Object
=======

```js
{
  id     : "8t..3r",    // 指定的 ID
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
  //---------------------------------------
  userId : "t6..8a",
  userName : "xiaobai",
  targetId : "t6..8a",
  targetName : "xyz.json",
  //---------------------------------------
  operation : "update",
  more : "{x:100,y:99}",
}
```

> @see `org.nutz.walnut.ext.entity.history.HisQuery`


示例
=======

```bash
# 根据一个复杂条件查询
cat demo_message.json | history query

# 仅仅获取某个指定 ID
history query '{id:"r5a..78q"}' -cqn
```