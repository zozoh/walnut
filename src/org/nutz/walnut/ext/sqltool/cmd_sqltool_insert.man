# 命令简介 

    `sqltool insert` 用来执行批量插入数据

# 用法
	
	sqltool xxx insert [$tableName] [-params '[{xxx:yyyy}, {...}]']

# 示例

```
:> sqltool hope insert ecom_jkxx -params '[{cfid:"xxxxxx20171026151836646125", cfrq:"2018-03-05 10:00:00", ghid:"20161026151836646125", hzxm:"测试A"}]'
[{
      "cfid": "xxxxxx20171026151836646125",
      "cfrq": "2018-03-05 10:00:00",
      "ghid": "20161026151836646125",
      "hzxm": "测试A",
      ".table": "ecom_jkxx"
}]
```