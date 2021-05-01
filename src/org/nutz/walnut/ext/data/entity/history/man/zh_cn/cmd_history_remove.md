命令简介
======= 

`history remove` 移除一组消息

用法
=======

```bash
history {HistoryName} remove
  [ID1, ID2 ...]      # 历史记录 ID
  [-cqn]              # JSON 输出格式
```

示例
=======

```bash
# 移除指定历史记录
demo:$ history remove 45..6a 8y..w2
```