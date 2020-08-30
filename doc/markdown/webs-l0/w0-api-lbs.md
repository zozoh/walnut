---
title: 地理及地址接口
author: zozohtnt@gmail.com
---

--------------------------------------
# 接口概览

  URL           |Method| Date | Description
----------------|------|------|----------
`lbs/countries` |`GET` |`json`| 获取全部国家列表和编码
`lbs/cn`        |`GET` |`json`| 获取中国国家行政区信息

--------------------------------------
# 接口详情

> 客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/lbs/countries`获取全部国家列表和编码

### 请求头

```bash
HTTP GET /api/lbs/countries
#---------------------------------
# Query String
# 国家名称语言，如果不传，默认位 "en_us"
lang : "zh_cn"
# 【选】 用名值对的形式输出，键为国家编码，值为国家的显示名
#  - obj  : 值为整个 country 对象
#  - name : 值为 country 的名称
# 如果不是 name 或者 obj 则无视
# 默认为空
map  : "name|obj"
```

### 响应成功(text/json)

```js
//------------------------------------------
// 如果不指定 map 参数
[{"key": "US", "name": "美国"},
 {"key": "AR", "name": "阿根廷"},
 ...]
//------------------------------------------
// 如果 map == name
{
  "US": "美国",
  "AR": "阿根廷",
  ...
}
//------------------------------------------
// 如果 map == obj
{
  "US": {"key": "US","name": "美国"},
  "AR": {"key": "AR","name": "阿根廷"},
  ...
}
```

### 响应失败

- `HTTP 500` : 内部错误

### 初始化脚本

```bash
# API: 获取全部国家列表和编码
@FILE .regapi/api/lbs/countries
{
  "http-header-Content-Type" : "text/json",
  "http-cross-origin" : "*"
}
%COPY:
lbs countries -lang ${http-qs-lang?en_us} -map ${http-qs-map?no} -cq 
%END%
```

--------------------------------------
## `/lbs/cn`获取中国国家行政区信息

### 请求头

```bash
HTTP GET /api/lbs/countries
#---------------------------------
# Query String
# 【选】地区编码下
# 如果指定，则会自动用 0 补齐到 6 位，即你传 11 和 110000 是一样的
# 默认的本值为空，表示获取顶级国家省自治区和直辖市
code : "210000"
# 【选】如果打开这个开关，则查询的是指定地址下的所有子地址
# 默认为 false 表示仅仅插叙指定地区编码的详情
# 当然，如果你连 code 也没传，即使本参数为 false 
# 本接口也只好返回顶级省份列表
list : true
```

### 响应成功(text/json)

```js
//------------------------------------------
// 如果不指定 code
[{
  "provinceType": 0,
  "level": 1,
  "noTown": false,
  "code": "210000",
  "name": "辽宁省",
  "province": "21",
  "provinceCode": "210000",
  "provinceName": "辽宁省"
}, {/*..*/} /*...*/]
//------------------------------------------
// 如果 code = "500101"
{
  "provinceType": 0,
  "level": 3,
  "noTown": false,
  "code": "500101",
  "name": "万州区",
  "province": "50",
  "provinceCode": "500000",
  "provinceName": "重庆市",
  "city": "01",
  "cityCode": "500100",
  "cityName": "市辖区",
  "area": "01",
  "areaCode": "500101",
  "areaName": "万州区"
}
//------------------------------------------
// 如果 code = "500101", list=true
[{
  "provinceType": 0,
  "level": 4,
  "noTown": true,
  "code": "500101001000",
  "name": "高笋塘街道",
  "province": "50",
  "provinceCode": "500000",
  "provinceName": "重庆市",
  "city": "01",
  "cityCode": "500100",
  "cityName": "市辖区",
  "area": "01",
  "areaCode": "500101",
  "areaName": "万州区",
  "town": "001000"
}, {/*..*/} /*...*/]
//------------------------------------------
// 如果 code = "hello"
null
```

### 响应失败

- `HTTP 500` : 内部错误

### 初始化脚本

```bash
# API: 获取中国国家行政区信息
@FILE .regapi/api/lbs/cn
{
  "http-header-Content-Type" : "text/json",
  "http-cross-origin" : "*"
}
%COPY:
lbs cn ${http-qs-code?} -list -cq -json
%END%
```