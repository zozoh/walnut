命令简介
======= 

`sql` 命令从 `sqltool`的复刻修改版。提供了对 SQL数据库完整的管理功能。
它依赖 `WnDataSourceService` 提供的数据源访问服务。

基本上，它假设你会在自己的域里存放某个数据源定义文件

```bash
~\
|-- .sql/                    # 配置文件目录
    |-- my-datasource-0.json # 描述了某个数据源
```

数据源描述文件格式为

```json
{
    "url" : "jdbc:mysql://127.0.0.1:3306/nutzbook",
    "username" : "root",
    "password" : "root"
}
```

用法
=======

```
sql {DataSource} exec       # 执行任意自定义SQL,最强大的命令了吧
sql {DataSource} insert     # 插入一组记录
sql {DataSource} reload     # 重新加载数据库配置文件,仅用于修改数据库配置文件后
```