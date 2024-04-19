---
title : 通用实体
author: zozoh
key: c1-gde
---

------------------------------------------
# 动机：为什么要有通用实体

在具体的业务开发场景中，你会发现总有一些数据对象，几乎是与项目无关的，
对它的增删改查又比较，怎么说呢，频繁。而且这些实体数量通常是巨大的，
以至于我们用一个普通的 `WnObj` 存放总觉得有点浪费。

这些实体可能是（包括但不限于）

- 点赞记录
- 收藏记录
- 打分记录
- 操作历史记录
- 评论留言
- 消息流中的消息

--------------------------------------
# 设计思路与边界

如果走文件夹映射到一张数据表，中间转了一层，总是会有一点性能损耗。
我们不如干脆用一个通用的命令将其操作封装。

这样，`RegApi` 直接就调用了，并不需要惊动 `WnIo`。

同时这样设计的好处，如果扩展到海量数据，直接修改命令服务即可。
当然，`WnIo`的实现层也是会考虑映射到海量数据的问题。
这个设计可能是底层还未充分考虑海量数据映射的时候，一个轻量级临时解决方案。

## 点赞/收藏/打分

数量庞大，且数据不是很关键，所以存放在`Redis`里

 Name   | Type         | Comments
--------|--------------|---------------
`like`  | `Set`        | 点赞
`favor` | `Sorted Set` | 收藏，分数为时间戳，以便排序
`score` | `Sorted Set` | 打分

## 操作历史/评论留言

数量庞大，但是数据可能会被花式检索，且要求较高的存储可靠性，所以存放在`SQL Table`里

 Name      | Modify | Comments
-----------|--------|---------------
`history`  | `No`   | 历史记录
`comment`  | `Yes`  | 评论留言
`newsfeed` | `No`   | 消息流信息

--------------------------------------
# 域文件布局

```bash
~/
#-------------------------------------
# 声明了本域所用到的所有数据源
|-- .dao/
|   |-- default.dao.json   # 默认数据源
#-------------------------------------
# 业务场景
|-- .domain/
|   |-- like/
|   |   |-- _like.json        # (Redis)点赞默认配置（同时也是Redis数据源）
|   |-- favor/
|   |   |-- _favor.json       # (Redis)收藏默认配置（同时也是Redis数据源）
|   |-- score/
|   |   |-- _score.json       # (Redis)打分默认配置（同时也是Redis数据源）
|   |-- history/
|   |   |-- _history.json     # (SQL)历史记录默认配置（指向 SQL 数据源）
|   |-- comment/
|   |   |-- _comment.json     # (SQL)评论默认配置（指向 SQL 数据源）
|   |-- newsfeed/
|   |   |-- _newsfeed.json    # (SQL)消息流默认配置（指向 SQL 数据源）
```

## Redis数据源格式

```js
{
  "host"  : "127.0.0.1",
  "port"  : 6379,
  "ssl"   : false,
  "password" : "123456",
  "database" : 0,
  "connectionTimeout": 2000,
  "soTimeout": 5000,
  "setup": {
    "prefix": "buy:"   // 用来规定键值前缀
  }
}
```

## SQL数据源格式

```js
{
  "url" : "jdbc:mysql://127.0.0.1:3306/walnut",
  "username" : "root",
  "password" : "root",
  "maxActive": 50,
  "maxWait" : 15000,
  "testWhileIdle" : true
}
```

--------------------------------------
# 点赞`like`

```bash
SADD {TargetID} {UID1} {UID2}
```

> **默认前缀**: `like:`

## 支持的操作

 Name      | Args               | Description
-----------|--------------------|-------------
 `yes`     | `TargetID`,`UID`   | 赞
 `no`      | `TargetID`,`UID`   | 取消赞
 `all`     | `TargetID`         | 谁在赞它
 `count`   | `TargetID`         | 有多少人赞
 `isLike`  | `TargetID`,`UID`   | 是否赞

--------------------------------------
# 收藏`favor`

```bash
ZADD {UID} AMS {TargetID} AMS {TargetID}
```

**默认前缀**: `favor:`

## 支持的操作

 Name       | Args                       | Description
------------|----------------------------|-------------
 `yes`      | `UID`, `TargetID`, `AMS`   | 添加收藏
 `no`       | `UID`, `TargetID`          | 取消收藏
 `all`      | `UID`, `rever`             | 全部的收藏
 `count`    | `UID`                      | 收藏了多少东西
 `when`     | `UID`, `TargetID`          | 收藏的时间

--------------------------------------
# 打分`score`

```bash
ZADD {TargetID} 75 {UID} 100 {UID}
SET   {sum:TargetID} 175
```

**默认前缀**: `score:`

## 支持的操作

 Name       | Args                       | Description
------------|----------------------------|-------------
 `it`       | `TargetID`,`UID`,`N`       | 打分（如果已经打分了，就不能再打了）
 `cancel`   | `TargetID`,`UID`           | 取消打分
 `all`      | `UID`                      | 全部打分的人
 `count`    | `TargetID`                 | 获取打分人数
 `sum`      | `TargetID`                 | 获取总分
 `resum`    | `TargetID`                 | 重新计算总分
 `get`      | `TargetID`,`UID`,`dft:-1`  | 获取具体分值

--------------------------------------
# 历史记录`SQL`

## 配置文件

```js
//--------------------------------------
// 数据源名称
"dao": "default",    // 默认用 default
//--------------------------------------
// 连接的表名
// 如果是分表，可以用 t_history_${ext3} 来表示名称
// 其中，占位符 ${id} 为实体本身的字段名(Java)
"tableName" : "t_history"
//--------------------------------------
// 下面定义了关键字段的长度，超出了要截取
"fieldSizes" : {
  id: 26,
  uid  : 26,
  unm  : 26,
  utp  : 20,
  tid  : 26,
  tnm  : 26,
  ttp  : 20,
  opt  : 50,
  mor  : 128
}
//--------------------------------------
// 指定了必须的字段，如果给定字段不存在，则放弃插入
requires : ["uid","tid","opt"]

```

## 数据结构

```bash
id: ID         # 记录唯一ID
#-------------------------------------
# 谁？
#-------------------------------------
uid  : ID      # 用户ID
unm  : "xxx"   # 【冗】用户名
utp  : "xxx"   # 【选】用户类型
#-------------------------------------
# 在什么时候？
#-------------------------------------
ct : AMS       # 记录创建时间，绝对毫秒数
#-------------------------------------
# 对什么？
#-------------------------------------
tid : ID       # 关联对象的 ID
tnm : "xxx"    # 【冗】关联对象名
ttp : "xxx"    # 【选】关联对象类型
#-------------------------------------
# 做了什么？
#-------------------------------------
opt : "xxx"    # 这个是动作名称
mor : "xxx"    # 关于动作的更多细节，譬如更新的字段值等
```

> 每个域一个`历史记录`表，配置在 `~/.domain/history/` 里


## 示例建表语句

```sql
CREATE TABLE `t_history` (
	`id` CHAR(64)  NOT NULL,
	`uid` CHAR(64) NOT NULL,
	`unm` CHAR(64) NULL DEFAULT NULL,
	`utp` CHAR(32) NULL DEFAULT NULL,
	`tid` CHAR(64) NULL DEFAULT NULL,
	`tnm` CHAR(64) NULL DEFAULT NULL,
	`ttp` CHAR(32) NULL DEFAULT NULL,
	`opt` VARCHAR(128) NOT NULL,
	`mor` text,
	`ct` BIGINT(20) UNSIGNED NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `UID` (`uid`),
	INDEX `TID` (`tid`),
	INDEX `CT` (`ct`),
	INDEX `UID_CT` (`uid`, `ct`),
	INDEX `TID_CT` (`tid`, `ct`)
)
COLLATE='utf8_general_ci'
ENGINE=MyISAM
;
```

ClikHouse 建表语句:
```sql
CREATE TABLE t_history (
    id String,
    uid String,
    unm String DEFAULT NULL,
    utp String DEFAULT NULL,
    tid String,
    tnm String DEFAULT NULL,
    ttp String DEFAULT NULL,
    opt String,
    mor String,
    ct Int64
) ENGINE = MergeTree()
ORDER BY (ct, id) 
PRIMARY KEY (ct, id) 
SETTINGS index_granularity = 8192;

CREATE INDEX idx_tid ON his_case (tid) TYPE minmax GRANULARITY 8192;
```

--------------------------------------
# 评论留言`SQL`

这里的`评论留言`并不是通常的网站上的评论留言，因为我们不考虑附件功能。
如果图文并茂的`盖楼`，应该另立一个`ThingSet`比较妥当。

这里的`评论留言`仅仅是文字性的，可以支持：

- Markdown格式文本
- `Emoji`

> 当然，你可以规定，所有的评论图片都按照某个约定存放在某个目录下。
> 这样，评论留言也能支持图片或者附件了

## 配置文件

```js
//--------------------------------------
// 数据源名称
"dao": "default",    // 默认用 default
//--------------------------------------
// 连接的表名
// 如果是分表，可以用 t_commnet_${ext3} 来表示名称
"tableName" : "t_comment"
//--------------------------------------
// 下面是扩展信息，可以任意定制
"setup": {..}
```

## 数据结构

```bash
#-------------------------------------
# 谁？
#-------------------------------------
uid  : ID      # 用户ID
unm  : "xxx"   # 【冗】用户名
avatar : SHA1  # 【冗】用户头像的 SHA1
#-------------------------------------
# 在什么时候？
#-------------------------------------
ct : AMS       # 记录创建时间，绝对毫秒数
#-------------------------------------
# 对什么？
#-------------------------------------
taid : ID      # 目标对象的 ID
tatp : "xxx"   # 【选】目标对象类型
#-------------------------------------
# 说了什么？
#
# !!! 这里内容可能会包括图片，等我把桶管理器想明白
# 这里关联一个桶管理器就是了
# 或许，整个数据集有一个存放图片的目录？
# 像 thing 一样存放?
#
#-------------------------------------
wdtp  : 1      # 内容类型: 0 纯文本, 1 Markdown, 2 富文本
words : "xxx"  # 评论的具体内容，支持 Markdown
```

> 每个域一个`评论`表，配置在 `~/.domain/comments/` 里

--------------------------------------
# 消息流`SQL`

由于信息流数据被假设为**巨大**，所以它将会操作某个 SQL 数据库中的表。 

它假设 `Walnut`每个域的可以为自己某个账户系统（一个账户表）可以开设多个信息流(`newsfeed`)，
信息流里面的信息可以分作下面三类：

 Type       | Name | Receiver | Example
------------|------|----------|----------------
`broadcast` | 广播 | 全部用户  | "明日，晴，气温23摄氏度"
`multicast` | 组播 | 全组用户  | "因为上海大众运动会，明天不上班"
`unicast`   | 单播 | 某个用户  | "R011机器人测试步骤已经完成"

> 为每个用户提供一个时间线，记录自己的能收到的消息

## 配置文件

```js
//--------------------------------------
// 数据源名称
"dao": "default",    // 默认用 default
//--------------------------------------
// 连接的表名
// 如果是分表，可以用 t_newsfeed_${ext3} 来表示名称
"tableName" : "t_newsfeed"
//--------------------------------------
// 扩展字段的映射信息
"setup": {
  "ext0" : "projId",
  "ext1" : "issueId",
  "ext2" : "checkListId"
}
```

## 数据结构

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

## 示例建表语句

```sql
CREATE TABLE `t_newsfeed` (
	`id` CHAR(26) NOT NULL,
	`tp` TINYINT(20) NULL DEFAULT NULL,
	`readed` TINYINT(1) NULL DEFAULT NULL,
	`stared` TINYINT(1) NULL DEFAULT NULL,
	`ct` BIGINT(64) NULL DEFAULT NULL,
	`rd_at` BIGINT(64) NULL DEFAULT NULL,
	`src_id` CHAR(26) NULL DEFAULT NULL,
	`src_tp` CHAR(20) NULL DEFAULT NULL,
	`ta_id` CHAR(26) NULL DEFAULT NULL,
	`ta_tp` CHAR(20) NULL DEFAULT NULL,
	`title` VARCHAR(128) NULL DEFAULT NULL,
	`content` VARCHAR(128) NULL DEFAULT NULL,
	`ext0` VARCHAR(128) NULL DEFAULT NULL,
	`ext1` VARCHAR(128) NULL DEFAULT NULL,
	`ext2` VARCHAR(128) NULL DEFAULT NULL,
	`ext3` VARCHAR(128) NULL DEFAULT NULL,
	`ext4` VARCHAR(128) NULL DEFAULT NULL,
	`ext5` VARCHAR(128) NULL DEFAULT NULL,
	`ext6` VARCHAR(128) NULL DEFAULT NULL,
	`ext7` VARCHAR(128) NULL DEFAULT NULL,
	`ext8` VARCHAR(128) NULL DEFAULT NULL,
	`ext9` VARCHAR(128) NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=MyISAM
;

```