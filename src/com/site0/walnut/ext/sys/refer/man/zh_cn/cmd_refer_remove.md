# 命令简介 

`refer remove` 将会为某个目标增加一个引用

# 用法

```bash
refer remove 
  [TargetId]        # 【必】引用者 ID
  [ReferId...]      # 【必】被引用者 ID，可多个
```

# 示例
    
```bash
# 为某自定义目标删除引用
demo:> refer remove $SHA1 $ID1 $ID2
```
