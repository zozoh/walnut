过滤器简介
======= 

`@fcc` 通过文件/目录/ThingSet向上下文增加一个或者多个抄送者
 

用法
=======

```bash
@fcc 
  [~/path/to]        # 文件/目录/ThingSet
  [-mapping {..}]    # 映射方式，默认 {name, email}
  [-match {..}]      # 过滤方式，如果未声明，对于目录/ThingSet 无视
  [-reset]           # 重置对应的抄送者列表
```


示例
=======

```bash
sendmail @fcc ~/accounts -match 'role:"ADMIN"' -mapping 'name:"=nickname",account:"=email"'
```

