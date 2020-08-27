---
title: 实体·收藏夹接口
author: zozohtnt@gmail.com
---

--------------------------------------
# 接口概览

  URL                |Method| Date | Description
---------------------|------|------|----------
`entity/favor/it`    |`GET` |`json`| 收藏/取消一个对象
`entity/favor/all`   |`GET` |`json`| 获取全部收藏（翻页）
`entity/favor/when`  |`GET` |`json`| 判断是否收藏（可多个）

--------------------------------------
# 接口详情

> 客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/entity/favor/it`收藏/取消一个对象

### 请求头

```bash
HTTP GET /api/entity/favor/it
#---------------------------------
# Query String
ticket : "4ih..23d"    # 会话票据
ta     : "xxx"         # 收藏对象（通常是一个ID）
#
# true 表示收藏, false 表示取消收藏
# 默认不传这个参数就是 true
md : true
#
# 收藏分类，这个不同的域可以指定不同的默认值
# 通常为 "pro" 表示商品。即你不传这个参数就
# 相当于是 "pro" 分类
# 这样，如果你的站点有多个东西需要收藏，可以指定
# 不同的收藏分类。
c : "pro"
```

### 响应成功(text/json)

```js
{
  "reading": 1598526759659
}
```

返回一个键值对，其中键就是 `favor/it` 你传入的 `ta`，
值是一个时间绝对毫秒数，表示收藏的时间

### 响应失败

- `HTTP 500` : 内部错误

### 初始化脚本

```bash
# API: 收藏：收藏（或者取消）一个对象
@FILE .regapi/api/entity/favor/it
{
   "http-header-Content-Type" : "text/json",
   "http-www-home" : "~/www/${domain}",
   "http-www-ticket" : "http-qs-ticket",
   "http-www-auth" : true,
   "http-cross-origin" : "*"
}
%COPY:
favor ${http-qs-md?yes} '${http-www-me-id}${http-qs-c?:pro}' ${http-qs-ta?nothing} -json -cqn -quiet;
favor when '${http-www-me-id}${http-qs-c?:pro}' ${http-qs-ta} -dv nil -ms -json -cqn;
%END%
```

--------------------------------------
## `/entity/favor/when`判断指定对象是否已收藏

### 请求头

```bash
HTTP GET /api/entity/favor/when
#---------------------------------
# Query String
ticket : "4ih..23d"    # 会话票据
#
# 用空格分隔的多个对象
# 表示你想要知道那些对象收藏了
ids : "book reading xxx"
#
# 收藏分类，这个不同的域可以指定不同的默认值
# 通常为 "pro" 表示商品。即你不传这个参数就
# 相当于是 "pro" 分类
# 这样，如果你的站点有多个东西需要收藏，可以指定
# 不同的收藏分类。
c : "pro"
```

### 响应成功(text/json)

```js
{
  "reading": 1598526759659
}
```

返回一个键值对，其中键就是 `favor/it` 你传入的 `ta`，
值是一个时间绝对毫秒数，表示收藏的时间

### 响应失败

- `HTTP 500` : 内部错误

### 初始化脚本

```bash
# API: 收藏：获取收藏信息
@FILE .regapi/api/entity/favor/when
{
   "http-header-Content-Type" : "text/json",
   "http-www-home" : "~/www/${domain}",
   "http-www-ticket" : "http-qs-ticket",
   "http-www-auth" : false,
   "http-cross-origin" : "*"
}
%COPY:
favor when '${http-www-me-id?anonymous}${http-qs-c?:pro}' ${http-qs-ids} -dv nil -ms -json -cq
%END%
```

--------------------------------------
## `/entity/favor/all`获取全部收藏

### 请求头

```bash
HTTP GET /api/entity/favor/all
#---------------------------------
# Query String
ticket : "4ih..23d"    # 会话票据
limit  : 100           # 最多获取的记录数，默认 100
skip   : 0             # 跳过的记录数，默认 0
by     : id            # 【选】如果指定了这个参数，则会为列表增加 "obj" 字段
```

### 响应成功(text/json)

```js
{
  // 这个列表列出了所有的收藏对象
  "list": [
      {"target": "reading", "time": 1598526759659, obj: {/*如果声明了by:id*/}},
      {"target": "movie", "time": 1598526754477, obj: {/*如果声明了by:id*/}},
      {"target": "book", "time": 1598526747339, obj: {/*如果声明了by:id*/}},
      {"target": "cake", "time": 1598526681534, obj: {/*如果声明了by:id*/}],
  // 翻页信息
  "pager": {
      "pn": 1,       // 当前第几页（1BASE）
      "pgsz": 100,   // 每页记录数
      "sum": 4,      // 一共多少记录
      "skip": 0,     // 跳过了多少记录
      "pgc": 1,      // 一共多少页
      "count": 4     // 当前页取得了多少记录
  }
}
```

### 响应失败

- `HTTP 500` : 内部错误

### 初始化脚本

```bash
@FILE .regapi/api/entity/favor/all
{
   "http-header-Content-Type" : "text/json",
   "http-www-home" : "~/www/${domain}",
   "http-www-ticket" : "http-qs-ticket",
   "http-www-auth" : true,
   "http-cross-origin" : "*"
}
%COPY:
favor all '${http-www-me-id?anonymous}${http-qs-c?:pro}' \
  -limit ${http-qs-limit?100} \
  -skip  ${http-qs-skip?0} \
  -rever -obj ${http-qs-by?nil} -pager -ms -json -cqn
%END%
```