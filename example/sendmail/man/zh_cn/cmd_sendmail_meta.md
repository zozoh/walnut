过滤器简介
======= 

`@meta` 通过文件元数据向上下文增加一组变量
 

用法
=======

```bash
@meta 
  [~/path1, ~/path2]      # 多个对象路径
  [-mapping {..}]         # 【选】映射方式，如果没有，则会去掉所有标准字段
```


示例
=======

```bash
# 指定元数据
sendmail @meta ~/myvars -mapping 'x:"=pos.x", y:"=pos.y"'
```

