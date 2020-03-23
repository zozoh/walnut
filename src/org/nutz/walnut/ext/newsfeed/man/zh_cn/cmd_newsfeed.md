命令简介
======= 

`newsfeed` 提供了对于信息流类数据的基本操作支持。由于信息流数据被假设为**巨大**，
所以它将会操作某个 SQL 数据库中的表。 
> 它依赖 `WnDataSourceService` 提供的数据源访问服务。

它假设 `Walnut`每个域的可以为自己某个账户系统（一个账户表）可以开设多个信息流(`newsfeed`)，
信息流里面的信息可以分作下面三类：

 Type       | Name | Receiver | Example
------------|------|----------|----------------
`broadcast` | 广播 | 全部用户  | "明日，晴，气温23摄氏度"
`multicast` | 组播 | 全组用户  | "因为上海大众运动会，明天不上班"
`unicast`   | 单播 | 某个用户  | "R011机器人测试步骤已经完成"

> 为每个用户提供一个时间线，记录自己的能收到的消息

基本上，它假设你会在自己的域里存放某个数据源定义文件

```bash
~/.domain/
# newsfeed 数据源定义目录
|-- newsfeed/
    |-- default.json  # 域默认newfeed数据源配置信息
    |-- red-blue.json # 名称为"red-blue"的newsfeed数据源
```

数据源定义文件
======= 

```js
{
  //--------------------------------------
  // 数据源名称，默认为配置文件主名
  "feedName" : "default",
  //--------------------------------------
  // 数据源连接信息
  "jdbcUrl" : "jdbc:mysql://127.0.0.1:3306/nutzbook",
  "jdbcUserName" : "root",
  "jdbcPassword" : "root",
  //--------------------------------------
  // 对应的账户表，默认为 "~/accounts"
  // "accounts" : "~/accounts",
  //--------------------------------------
  // 数据表, 默认为 "t_newsfeed"
  "tableName" : "t_newsfeed"
  //--------------------------------------
  // 扩展字段的映射信息
  "extFieldsMapping" : {
    "ext0" : "projId",
    "ext1" : "issueId",
    "ext2" : "checkListId"
  }
}
```

SQL表数据结构
======= 

```bash
# TABLE: t_newsfeed
#------------------------------------------
# 索引
- `id` : Primary ID : 直接获取某条消息
- `dist_id+read+star` : Combo Key : 查自己已读未读消息
- `ct` : Normal Index : 按时间排序场景
#------------------------------------------
# 消息标识
id : UUID  # 消息唯一 ID
tp : 0,    # 消息类型 0:broadcast, 1:projcast, 2:unicast
#------------------------------------------
# 关联实体
# 预留 10 个扩展字段
ext0 : xxx            
ext1 : xxx
...
ext7 : xxx
ext8 : xxx
#------------------------------------------
# 状态: 联合索引 read-star 将用来快速获取可清除消息
readed  : 0            # 消息已读状态: 0.未读; 1.已读
stared  : 0            # 消息标记状态: 0.普通; 1.加星
#------------------------------------------
# 时间戳
ct    : AMS          # 消息创建时间
rd_at : AMS          # 用户标记已读的时间
#------------------------------------------
# 消息的分发
src_id : UUID        # 消息是由哪个实体的操作而导致发起的
src_tp : "user"      # 消息源的类型
ta_id  : UUID        # 消息的目标对象 ID
ta_tp  : "user"      # 消息目标的类型
#------------------------------------------
# 消息正文
title   : "xxx"       # 消息标题
content : "XXX"       # 消息正文
# ctype   : "text"      # 消息正文内容类型, text|html|markdown
#------------------------------------------
```
> 创建时 `cmd_newsfeed` 会自动补全消息的 `tp|ct|readed|stared`字段

用法
=======

```bash
# 子命令
newsfeed {FeedName} add      # 添加消息
newsfeed {FeedName} query    # 获取某指定条件的消息
newsfeed {FeedName} remove   # 删除消息
newsfeed {FeedName} readed   # 标记消息已读/未读
newsfeed {FeedName} stared   # 给消息加/减星
newsfeed {FeedName} clean    # 清除所有已读消息

# 为默认的 newsfeed 添加一条信息
cat demo_message.json | newsfeed add
```