# 命令简介 

    `thing sql` 本命令并不存在,这是一份文档

# 用法

```bash
tp              : "thing_set", # 当前目录是个thing目录
thing-by        : "sql"      , # 使用SQL实现
thing-sql-ds    : "sqlthing" , # 数据源名称, 对应 ~/.sqlthing/${thing-sql-ds}/conf
thing-sql-table : "t_user"     # 对应的数据表名称
```

## 数据表的必选字段

```
id        varchar(26)      主键
nm        varchar(128)     字符主键
ct        bigint           创建时间
lm        bigint           最后修改时间
th_live   tinyint          是否存活(会解析成true/false)
```

## 参考建表语句

```sql
CREATE TABLE `t_user` (
`id` varchar(26) NOT NULL,
`nm` varchar(26) NOT NULL,
`ct` bigint(20) DEFAULT '0',
`lm` bigint(20) DEFAULT NULL,
`age` int(11) DEFAULT NULL,
`city` varchar(45) DEFAULT NULL,
`th_live` tinyint(1) DEFAULT '1',
PRIMARY KEY (`id`),
UNIQUE KEY `nm_UNIQUE` (`nm`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
```

## 参考配置文件

```json
{
   "username" : "root",
   "password" : "root",
   "jdbcUrl"  : "jdbc:mysql://127.0.0.1/sqlthing"
}
```