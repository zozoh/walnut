---
title: 接口·全局基础
author: zozohtnt@gmail.com
key: w0-api-base
---

--------------------------------------
# 接口概览

  URL   | Method| Date | Description
--------|-------|------|----------
`thumb` | `GET` |`img` | 获取对象缩略图
`media` | `GET` |`img` | 获取对象本身
`read`  | `GET` |`img` | 读取对象内容
`objs`  | `GET` |`json`| 我的收货地址

--------------------------------------
# 接口详情

> 客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/auth/thumb`获取对象内容

### 请求头

```bash
HTTP GET /api/thumb
#---------------------------------
# Query String
# 对象 ID，注意！，整个 query string 为 id:xxx
id:xxxx
```

### 响应成功(image/xxx)

```
FF D8 FF E1 00 18 45 78 69 66 00 00 49 49 2A 00
08 00 00 00 00 00 00 00 00 00 00 00 FF EC 00 11
44 75 63 6B 79 00 01 00 04 00 00 00 50 00 00 FF
...
```

### 响应失败

- `HTTP 404` : 找不到文件
- `HTTP 500` : 内部错误

### 初始化脚本

```bash
# API: 根据对象ID获取其缩略图
@FILE .regapi/api/thumb
{
   "http-dynamic-header": true,
   "http-cross-origin" : "*"
}
%COPY:
httpout -body `obj "${http-qs}" -e "^(thumb)$" -V` \
  -etag  '${http-header-IF-NONE-MATCH?none}' \
  -range '${http-header-RANGE?}'
%END%
```

--------------------------------------
## `/auth/media`获取对象本身

### 请求头

```bash
HTTP GET /api/media
#---------------------------------
# Query String
# 对象 ID，注意！，整个 query string 为 id:xxx
id:xxxx
```

### 响应成功(image/xxx)

```
FF D8 FF E1 00 18 45 78 69 66 00 00 49 49 2A 00
08 00 00 00 00 00 00 00 00 00 00 00 FF EC 00 11
44 75 63 6B 79 00 01 00 04 00 00 00 50 00 00 FF
...
```

### 响应失败

- `HTTP 404` : 找不到文件
- `HTTP 500` : 内部错误

### 初始化脚本

```bash
# API(thing): 缩略图
@FILE .regapi/api/media
{
   "http-dynamic-header": true,
   "http-cross-origin" : "*"
}
%COPY:
httpout -body ${http-qs} \
  -etag  '${http-header-IF-NONE-MATCH?none}' \
  -range '${http-header-RANGE?}'
%END%
```

--------------------------------------
## `/auth/read`读取内容

### 请求头

```bash
HTTP GET /api/media
#---------------------------------
# Query String
f : "xxxx/xx..xx"    # 某对象的 SHA1 指纹
mime : "image/jpeg"  #【选】响应的内容类型
dwnm : "xxx.xx"      #【选】指定下载文件的名称
```

> 支持范围下载，支持 `ETag`

### 响应成功

```
FF D8 FF E1 00 18 45 78 69 66 00 00 49 49 2A 00
08 00 00 00 00 00 00 00 00 00 00 00 FF EC 00 11
44 75 63 6B 79 00 01 00 04 00 00 00 50 00 00 FF
...
```

### 响应失败

- `HTTP 404` : 找不到文件
- `HTTP 500` : 内部错误

### 初始化脚本

```bash
# API(thing): 直接读取内容
@FILE .regapi/api/read
{
   "http-dynamic-header": true,
   "http-cross-origin" : "*"
}
%COPY:
httpout -body 'sha1:${http-qs-f}' \
  -mime  '${http-qs-mime?}' \
  -download '${http-qs-dwnm?}' \
  -etag  '${http-header-IF-NONE-MATCH?none}' \
  -range '${http-header-RANGE?}'
%END%
```

--------------------------------------
## `/auth/objs`读取对象列表

### 请求头

```bash
HTTP GET /api/objs
#---------------------------------
# Query String
phs : "id:xxx,id:xxx..."  # 对象路径列表
```

> 支持范围下载，支持 `ETag`

### 响应成功(JSON)

```js
[{
  "id" : "xxx",
  "nm" : "xxx",
  ...
}]
```

### 响应失败

```js
{
  ok : false,
  errCode : "e.auth.ticked.noexist",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.io.noexists` : 对象不存在

### 初始化脚本

```bash
# API(OBJ): 获取多个指定对象元数据
@FILE .regapi/api/objs
{
  "http-header-Content-Type" : "text/json",
  "http-cross-origin" : "*"
}
%COPY:
obj ${http-qs-phs} -cqnl 2>&1 | ajaxre -cqn
%END%
```