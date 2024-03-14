命令简介
======= 

`hookx` 是一个用来处理钩子相关的命令

钩子支持的动作包括 `write|create|delete|meta|mount|move`
    

用法
=======

```
hookx [[@filter filter-args...]...]
```

它支持的过滤器有：

```bash
@get      # 为上下文添加一个符合条件的对象
@query    # 为上下文添加多个符合条件的对象
@keys     # 为上下文对象添加更新键名，以便模拟更新操作
@invoke   # 对上下文所有对象执行某种操作的钩子
@view     # 对上下文所有对象检查执行某种操作的钩子
```
