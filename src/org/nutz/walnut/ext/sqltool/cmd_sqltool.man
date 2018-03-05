# 命令简介 

    `sqltool` 用来管理连接到SQL数据库

# 用法

```
sqltool xxx exec       # 执行任意自定义SQL,最强大的命令了吧
sqltool xxx insert     # 插入一组记录
sqltool xxx reload     # 重新加载数据库配置文件,仅用于修改数据库配置文件后
```

# xxx对应的数据库配置文件

```
~/.sqltool/xxx/dataSource
```

格式如下

```
{
	url : "jdbc:mysql://127.0.0.1:3306/nutzbook",
	username : "root",
	password : "root"
}
```