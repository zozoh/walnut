# 命令简介 

    `sqltool exec` 用来执行自定义SQL

# 用法
	
	sqltool xxx exec [$sql] [-params '{xxx:yyyy}'] [-vars '{aaa:bbb}'] [-limit 1] [-skip 1] [-force_query true]

# 示例


## 查询有哪些表

强制查询模式

```
>: sqltool xxx exec 'show tables' -force_query true
{
   "pager: null,
   "list": [{
      "Tables_in_nutzbook": "t_user"
   }]
}
```

## 查询具有某个名字的用户

使用了参数占位符 @ 并分页

```
>: sqltool xxx exec 'select * from t_user where nm=@name' -params '{name:"wendal"}' -limit 1 -skip 0 -pager
{
   "pager":  {
   		
   },
   "list": [{
      "id" : 123,
      "nm" : "wendal",
      "age" : 123,
      "loc" : "gz"
   }]
}
```

## 插入数据

```
:> sqltool xxx exec 'insert into t_user(nm, age) values(@name, @age)' -params '{name:"wendal", age:22}'
{ok:true}
```

## 更新数据

```
:> sqltool xxx exec 'update t_user set age=age+1 where nm=@name' -params '{name:"wendal"}'
{changed:1}
```

## 删除数据

```
:> sqltool xxx exec 'delete from t_user where nm=@name' -params '{name:"wendal"}'
{changed:1}
```