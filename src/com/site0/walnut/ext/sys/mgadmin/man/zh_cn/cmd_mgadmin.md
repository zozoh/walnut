# 命令简介 

    `mgadmin` 用来直接与mongodb进行交互
    !!! 仅root组管理员才可以使用

用法
=======

```
mgadmin profile           # 性能监视器的相关操作
mgadmin index             # 索引的相关操作
mgadmin raw_query         # 原生查询
mgadmin raw_count         # 原生统计
mgadmin raw_explain       # 查询执行计划
mgadmin sanity_check      # 完整性检查
```

raw_count与raw_explain的参数与raw_query是一样的,只是输出不一样