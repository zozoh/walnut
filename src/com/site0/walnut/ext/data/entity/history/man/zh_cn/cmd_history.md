命令简介
=======

`history` 提供了对于通用历史记录的基本操作支持。

> 关于历史记录更多详情，请参看 `core-l1/c1-general-data-entity.md`


用法
=======

```bash
# 子命令
history {HistoryName} add      # 添加历史记录
history {HistoryName} get      # 获取某一条指定的历史记录
history {HistoryName} query    # 获取某指定条件的历史记录
history {HistoryName} remove   # 删除一条或多条指定的历史记录
history {HistoryName} clean    # 清除（按条件）历史记录
history {HistoryName} fake     # 自动创建假的历史记录

# 为默认的 history 添加一条记录
cat history.json | history add
```