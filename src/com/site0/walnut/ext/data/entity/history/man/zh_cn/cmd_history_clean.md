命令简介
======= 

`history clean` 清除指定消息接收者所有已读的普通消息

用法
=======

```bash
history {HistoryName} clean
  [Query Object]            # 复杂查询条件对象
  [-all]                    # 显示的删除所有记录
                            # 安全起见，若用户忘记指定删除条件
                            # 必须显式的声明本参数，才能删除所有记录
```

示例
=======

```bash
# 清除全部历史记录 
demo:$ history clean -all
271234

# 清除某用户的全部历史记录
demo:$ history clean '{userId:"aq..21"}'
571
```