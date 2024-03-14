命令简介
======= 

`history get` 获取某一条历史记录详情

用法
=======

```bash
history {HistoryName} get
  [ID]        # 历史记录对象
  [-cqn]      # JSON 输出格式
```

示例
=======

```bash
# 添加一条历史记录
demo:$ history get "451..u1a"
{
  id: "451..u1a",
  ...
}
```