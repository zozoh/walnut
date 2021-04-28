命令简介
======= 

`history clean` 清除指定消息接收者所有已读的普通消息

用法
=======

```bash
history {HistoryName} clean
  [Query Object]            # 复杂查询条件对象
```

示例
=======

```bash
# 清除全部历史记录 
demo:$ history clean
271234

# 清除某用户的全部历史记录
demo:$ history clean '{userId:"aq..21"}'
571
```