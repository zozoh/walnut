# 命令简介 

    `mgadmin sanity_check` 用于校验数据库

用法
=======

```
mgadin sanity_check
```

当前支持的检查项:

* 缺id
* 缺pid
* 有pid但不存在pid对应的记录
* d0和d1与事实不匹配

示例
=======

通过mongo客户端插入几条非法数据后,检查结果如下

```
root:test# mgadmin sanity_check
record without pid?!: { "_id" : { "$oid" : "5a3b33fa0c24c0dbba962b1c"} , "d0" : "home" , "d1" : "wendal"}
record with unkown pid?!: { "_id" : { "$oid" : "5a3b34160c24c0dbba962b1e"} , "d0" : "home" , "d1" : "wendal" , "pid" : "ABC" , "id" : "123"}
checked 3 DBObjects
```