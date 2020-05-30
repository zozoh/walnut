命令简介
======= 

`history add` 添加一条历史记录

用法
=======

```bash
history {HistoryName} add
  [{History Object}]        # 历史记录对象
  [-cqn]                    # JSON 输出格式
```

Newsfeed Object
=======

```js
{
  id     : "8t..3r",    // 【自动】唯一标识
  //---------------------------------------
  createTime: AMS,      // 【自动】创建时间
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

> @see `org.nutz.walnut.ext.entity.history.HistoryRecord`


示例
=======

```bash
# 添加一条历史记录
history add '{userId:"ya..91",targetId:"89..6y",operation:"update"}'
{
  id: "451..u1a",
  ...
}

# 添加一条比较复杂的历史记录
demo:$ cat demo_history.json | history add -cqn
{
  id: "892..e1a",
  ...
}
```