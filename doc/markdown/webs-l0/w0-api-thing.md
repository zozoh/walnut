---
title: 接口·通用数据集
author: zozohtnt@gmail.com
---

--------------------------------------
# 接口概览

  URL              |Method| Date | Description
-------------------|------|------|----------
`thing/create`     |`POST`|`json`| 创建数据记录
`thing/delete`     |`GET` |`json`| 删除数据数据
`thing/get`        |`GET` |`json`| 获取记录元数据
`thing/list`       |`GET` |`json`| 列出全部数据记录
`thing/query`      |`GET` |`json`| 查询数据记录
`thing/update`     |`POST`|`json`| 更新数据记录
`thing/file/add`   |`GET` |`json`| 上传附件
`thing/file/get`   |`GET` |`json`| 获取附件元数据
`thing/file/read`  |`GET` |`bin` | 获取附件内容
`thing/file/remove`|`GET` |`json`| 删除附件

--------------------------------------
# 接口详情

> 客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/thing/create`创建数据记录

### 请求头

```bash
HTTP POST /api/thing/create
#---------------------------------
# Query String
ticket : "34t6..8aq1"     # 【必】登录会话的票据
ts : "~/xxx"              # 【必】数据集路径
```

### 请求体:JSON

> 新创建的数据记录元数据

```js
{
  // ..
}
```

### 响应成功(JSON)

```js
{
  // 数据对象 JSON
}
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/delete`删除数据数据

### 请求头

```bash
HTTP GET /api/thing/delete
#---------------------------------
# Query String
ticket : "34t6..8aq1"     #【必】登录会话的票据
ts   : "~/xxx"            #【必】数据集路径
id   : "5y..8q"           #【必】数据ID
hard : false              #【选】是否硬删除，默认 false
```

### 响应成功(JSON)

```js
{
  // 被删除数据对象 JSON
}
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/get`获取记录元数据

### 请求头

```bash
HTTP GET /api/thing/get
#---------------------------------
# Query String
ticket : "34t6..8aq1"     #【必】登录会话的票据
ts   : "~/xxx"            #【必】数据集路径
id   : "5y..8q"           #【必】数据ID
```

### 响应成功(JSON)

```js
{
  // 数据对象 JSON
}
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/list`列出全部数据记录

### 请求头

```bash
HTTP GET /api/thing/list
#---------------------------------
# Query String
ticket : "34t6..8aq1"   #【必】登录会话的票据
ts     : "~/xxx"            #【必】数据集路径
m      : "{..}"             #【选】过滤条件
sort   : "{ct:-1,nm:1}"     #【选】排序
e      : "^(id|nm|title)$"  #【选】过滤输出字段
limit  : 100                #【选】最大记录数（默认100）
skip   : 0                  #【选】跳过记录数（默认 0）
```

### 响应成功(JSON)

```js
[{
  // 数据对象 JSON
}]
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/query`查询数据记录

### 请求头

```bash
HTTP GET /api/thing/query
#---------------------------------
# Query String
ticket : "34t6..8aq1"   #【必】登录会话的票据
ts     : "~/xxx"            #【必】数据集路径
m      : "{..}"             #【选】过滤条件
sort   : "{ct:-1,nm:1}"     #【选】排序
e      : "^(id|nm|title)$"  #【选】过滤输出字段
limit  : 100                #【选】最大记录数（默认100）
skip   : 0                  #【选】跳过记录数（默认 0）
```

### 响应成功(JSON)

```js
{
  // 本页数据列表
  "list": [{
        // 数据对象 JSON
      }],
  // 翻页信息
  "pager": {
    pn    : 1,    // 当前页码（1base） 
    pgsz  : 100,  // 页大小
    sum   : 324,  // 总记录数
    skip  : 0,    // 跳过的记录数
    pgc   : 4,    // 总页数
    count : 100   // 当前页的记录数量
  }
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/update`更新数据记录

### 请求头

```bash
HTTP POST /api/thing/update
#---------------------------------
# Query String
ticket : "34t6..8aq1"     #【必】登录会话的票据
ts : "~/xxx"              #【必】数据集路径
id   : "5y..8q"           #【必】数据ID
```

### 请求体:JSON

> 新创建的数据记录元数据

```js
{
  // 要更新的对象元数据
}
```

### 响应成功(JSON)

```js
{
  // 数据对象 JSON
}
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/file/add`上传附件

### 请求头

```bash
HTTP POST /api/thing/file/add
#---------------------------------
# Query String
ticket : "34t6..8aq1"   #【必】登录会话的票据
ts   : "~/xxx"          #【必】数据集路径
id   : "5y..8q"         #【必】数据ID
d    : "media"          #【必】数据目录名称
fnm  : "a.jpg"          #【必】文件名
# 文件重名时是否覆盖，如果不覆盖，必须声明下面的
# 参数，以便得到新文件名
ow   : false
# 重名时如何修改文件名
# 譬如文件 abc.jpg，如果重名，会采用模板，模板的占位符为
#  @{marjor} : 文件主名 "abc"
#  @{suffix} : 文件后缀名 ".jpg"
#  @{nb} : 重名文件数
dupp : "@{major}(@{nb})@{suffix}"
```

### 请求体

> 就是本地文件的二进制流

```
FF D8 FF E1 00 18 45 78 69 66 00 00 49 49 2A 00
08 00 00 00 00 00 00 00 00 00 00 00 FF EC 00 11
44 75 63 6B 79 00 01 00 04 00 00 00 50 00 00 FF
...
```

### 响应成功(JSON)

```js
{
  // 数据对象 JSON
}
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/file/get`获取附件元数据

### 请求头

```bash
HTTP GET /api/thing/file/get
#---------------------------------
# Query String
ts   : "~/xxx"          #【必】数据集路径
id   : "5y..8q"         #【必】数据ID
d    : "media"          #【必】数据目录名称
fnm  : "a.jpg"          #【必】文件名
```

### 响应成功(JSON)

```js
{
  // ... 数据对象元数据
}
```

### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/file/read`获取附件内容

### 请求头

```bash
HTTP GET /api/thing/file/read
#---------------------------------
# Query String
ts   : "~/xxx"          #【必】数据集路径
id   : "5y..8q"         #【必】数据ID
d    : "media"          #【必】数据目录名称
fnm  : "a.jpg"          #【必】文件名
dw   : false            #【选】指定需要下载
```

### 响应成功(image/xxx)

```
FF D8 FF E1 00 18 45 78 69 66 00 00 49 49 2A 00
08 00 00 00 00 00 00 00 00 00 00 00 FF EC 00 11
44 75 63 6B 79 00 01 00 04 00 00 00 50 00 00 FF
...
```
### 响应失败

- `HTTP 500` : 内部错误

--------------------------------------
## `/thing/file/remove`删除附件

### 请求头

```bash
HTTP GET /api/thing/file/remove
#---------------------------------
# Query String
ts   : "~/xxx"          #【必】数据集路径
id   : "5y..8q"         #【必】数据ID
d    : "media"          #【必】数据目录名称
fnm  : "a.jpg"          #【必】文件名
```

### 响应成功(JSON)

```js
{
  // ... 被删除的数据对象元数据
}
```

### 响应失败

- `HTTP 500` : 内部错误