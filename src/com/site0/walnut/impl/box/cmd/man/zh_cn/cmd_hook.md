# 命令简介 

`hook` 是钩子相关的命令

# 用法

```bash
hook [-get o]             # 获取某个对象的钩子信息 
    [ACTION -do o]        # 重新执行对象的的某一钩子
    [ACTION -all {Query}]
    [-v]                  # 显示对每个对象的执行
```

其中 `ACTION` 可以是 `write|create|delete|meta|mount|move`
    
# 示例

```bash
# 列出一个对象 write 操作的钩子
hook write -get id:45dcfa

# 对象重新执行一遍钩子程序
hook write -do ~/abc.txt

# 列出一个对象所有操作的钩子
hook -get ~/abc.txt
```