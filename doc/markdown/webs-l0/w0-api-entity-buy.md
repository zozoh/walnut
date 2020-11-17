---
title: 接口·实体·购物车
author: zozohtnt@gmail.com
key: w0-api-en-buy
---

--------------------------------------
# 接口概览

  URL                |Method| Date | Description
---------------------|------|------|----------
`entity/buy/all`     |`GET` |`json`| 获取购物全部商品
`entity/buy/clean`   |`GET` |`json`| 清空购物车
`entity/buy/it`      |`GET` |`json`| 加减购物车商品
`entity/buy/rm`      |`GET` |`json`| 从购物车删除

--------------------------------------
# 接口详情

> 客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/entity/buy/it`加减购物车商品

### 请求头

```bash
HTTP GET /api/entity/buy/it
#---------------------------------
# Query String
ticket : "4ih..23d"    # 会话票据
id     : "xxx"         # 商品ID
#
# 购买数量，默认为 1
n : 1
#
# 表示如何应用购买数量(n):
#  - false : 表示增量， n=-1 表示减去一个商品
#  - true  : 表示数量， n=2 表示将商品数量直接设置为 2
# 默认（不传参）为 false，即 n 为增量
r : false
```

### 响应成功(text/json)

```js
{
  "ok": true,
  "data": [{
      "name": "book",
      "count": 1,
      "obj": {/*商品元数据*/}
    }, {
      "name": "beer",
      "count": 2,
      "obj": {/*商品元数据*/}
    }]
}
```

### 响应失败

- `HTTP 500` : 内部错误

```js
{
  "ok": false,
  "errCode": "xx.xxx.xx",
  "msg": "xxxxx"
}
```

### 初始化脚本

```bash
# API: 购物车： 购买/取消商品
@FILE .regapi/api/entity/buy/it
{
   "http-header-Content-Type" : "text/json",
   "http-www-home" : "~/www/${domain}",
   "http-www-ticket" : "http-qs-ticket",
   "http-www-auth" : false,
   "http-cross-origin" : "*"
}
%COPY:
buy it ${http-www-me-id?anonymous} '${http-qs-id?nil}' \
  ${http-qs-n?1} ${http-qs-r<boolean:-reset>?} -quiet;
buy all ${http-www-me-id?anonymous} -obj -ajax -cqn;
%END%
```

--------------------------------------
## `/entity/buy/rm`从购物车删除

### 请求头

```bash
HTTP GET /api/entity/buy/rm
#---------------------------------
# Query String
ticket : "4ih..23d"      # 会话票据
ids    : "ID1,ID2,ID3"   # 商品ID，半角逗号分隔
```

### 响应成功(text/json)

```js
{
    "ok": true,
    "data": 3   // 表示实际移除了多少种商品
}
```

### 响应失败

- `HTTP 500` : 内部错误

```js
{
  "ok": false,
  "errCode": "xx.xxx.xx",
  "msg": "xxxxx"
}
```

### 初始化脚本

```bash
# API: 购物车：删除商品
@FILE .regapi/api/entity/buy/rm
{
   "http-header-Content-Type" : "text/json",
   "http-www-home" : "~/www/${domain}",
   "http-www-ticket" : "http-qs-ticket",
   "http-www-auth" : true,
   "http-cross-origin" : "*"
}
%COPY:
buy rm ${http-www-me-id?anonymous} '${http-qs-ids?nil}' -ajax -cqn
%END%
```

--------------------------------------
## `/entity/buy/all`获取购物全部商品

### 请求头

```bash
HTTP GET /api/entity/buy/all
#---------------------------------
# Query String
ticket : "4ih..23d"      # 会话票据
```

### 响应成功(text/json)

```js
{
  "ok": true,
  "errCode": null,
  "msg": null,
  "data": [{
      "name": "book",
      "count": 1,
      "obj": {/*商品元数据*/}
    }, {
      "name": "beer",
      "count": 2,
      "obj": {/*商品元数据*/}
    }]
}
```

### 响应失败

- `HTTP 500` : 内部错误

```js
{
  "ok": false,
  "errCode": "xx.xxx.xx",
  "msg": "xxxxx"
}
```

### 初始化脚本

```bash
# API: 购物车：获取全部商品
@FILE .regapi/api/entity/buy/all
{
   "http-header-Content-Type" : "text/json",
   "http-www-home" : "~/www/${domain}",
   "http-www-ticket" : "http-qs-ticket",
   "http-www-auth" : true,
   "http-cross-origin" : "*"
}
%COPY:
buy all ${http-www-me-id?anonymous} -obj -ajax -cqn
%END%
```

--------------------------------------
## `/entity/buy/clean`清空购物车

### 请求头

```bash
HTTP GET /api/entity/buy/clean
#---------------------------------
# Query String
ticket : "4ih..23d"      # 会话票据
```

### 响应成功(text/json)

```js
{
    "ok": true,
    "data": true
}
```

### 响应失败

- `HTTP 500` : 内部错误

```js
{
  "ok": false,
  "errCode": "xx.xxx.xx",
  "msg": "xxxxx"
}
```

### 初始化脚本

```bash
# API: 购物车：清空
@FILE .regapi/api/entity/buy/clean
{
   "http-header-Content-Type" : "text/json",
   "http-www-home" : "~/www/${domain}",
   "http-www-ticket" : "http-qs-ticket",
   "http-www-auth" : true,
   "http-cross-origin" : "*"
}
%COPY:
buy clean ${http-www-me-id?anonymous} -ajax -cqn
%END%
```