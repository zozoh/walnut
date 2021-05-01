# 命令简介 

    `mgadmin raw_query` 使用mongodb的原生API查询数据

用法
=======

```
mgadin raw_query [-co 集合名称] [-limit 100] [-skip 0] <查询条件>
```

参数说明:
* co 集合名称,默认为obj
* limit 输出的结果数,默认100
* skip 跳过多少条记录,默认0

查询条件是json字符串

示例
=======

列出pid为xxxxxxx的数据

```
#> mgadmin raw_query '{pid:xxxxxxx}'
// count=25
[
{
	id : "....",
	pid : "xxxxxxx"
},
{
	id : "....",
	pid : "xxxxxxx"
},
...
]
```