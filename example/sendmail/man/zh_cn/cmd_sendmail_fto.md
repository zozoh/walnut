过滤器简介
======= 

`@fto` 通过文件/目录/ThingSet向上下文增加一个或者多个接收者
 

用法
=======

```bash
@fto 
  [~/path/to]        # 文件/目录/ThingSet
  [-mapping {..}]    # 映射方式，默认 {name, email}
  [-match {..}]      # 过滤方式，如果未声明，对于目录/ThingSet 无视
  [-reset]           # 重置对应的接收者列表
```


示例
=======

```bash
sendmail @fto ~/accounts -match 'role:"ADMIN"' -mapping 'name:"=nickname",account:"=email"'
```

