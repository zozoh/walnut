# 命令简介 

    `mgadmin index` 用于操作索引

用法
=======

```
mgadin index [-co 集合名称] [create|drop|cbasic]
```

示例
=======

列出索引,默认是obj集合

```
#> mgadmin index
{ "v" : 1 , "key" : { "_id" : 1} , "name" : "_id_" , "ns" : "walnut.obj"}
```

列出指定集合的索引

```
#> mgadmin index -co bucket
{ "v" : 1 , "key" : { "_id" : 1} , "name" : "_id_" , "ns" : "walnut.bucket"}
```

创建默认索引,将会为obj和bucket创建基础索引

```
#> mgadmin index cbasic
```

创建索引,分别是字段列表和选项, 选项可以不传

```
#> mgadmin index create '{d0:1,d1:1,wsid:1}' 'sparse:true'
#> mgadmin index create '{d0:1,d1:1,wsid:1}'
#
# 常用的建立索引语句
mgadmin index create '{"id":1}' '{"name":"OBJ_ID"}'
mgadmin index create '{"d0":1,"d1":1}' '{"name":"D0_D1"}'
mgadmin index create '{"d0":1,"d1":1,"tp":1}' '{"name":"D0_D1_TP"}'
mgadmin index create '{"pid":1}' '{"name":"PID"}'
mgadmin index create '{"pid":1,"nm":1}' '{"name":"PID_NM"}'
mgadmin index create '{"pid":1,"tp":1}' '{"name":"PID_TP"}'
mgadmin index create '{"pid":1,"ct":1}' '{"name":"PID_CT"}'
mgadmin index create '{"pid":1,"sort":1}' '{"sparse":true,"name":"PID_SORT"}'
```

删除索引. 通过索引的名称删除,列出索引的时候可以看到name

```
#> mgadmin index drop id_1
```