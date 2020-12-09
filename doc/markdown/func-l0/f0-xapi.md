---
title: 通用第三方API整合
author: zozoh
---

--------------------------------------
# 动机：为什么要有通用第三方API整合

以前通常的办法是为每个第三方 API 提供一个 SDK。
但是维护这个 SDK 代价非常高。

考虑到所谓`第三方API`，也就是一些 HTTP 接口，通常都需要一个访问密钥。
因此我们考虑应该可以做成一个比较通用的接口，提供下面三种方法：

- 根据配置文件管理访问密钥
- 根据映射配置，向第三方 HTTP 接口发起请求
- 处理接口返回的格式化数据

--------------------------------------
# 计划应用场景

我们有如下假定：

1. 第三方服务采用 HTTP 的方式提供
2. 访问服务通常需一个访问密钥
3. 提交的请求主要是:
   - `GET`
   - `POST`
4. 对于带 `body` 的请求，请求体的编码可能是:

 Name   | Title     | Mime
--------|-----------|--------
`form`  | 普通表单[【默认】 | `application/x-www-form-urlencoded`
`files` | 文件流表单 | `multipart/form-data`
`json`  | JSON      | `application/json`
`xml`   | XML       | `text/xml`
`text`  | 纯文本    | `text/plain`
`bin`   | 二进制    | `application/octet-stream`

5. 对于响应，我们认为其内容可能是:

 Name   | Title     | Mime
--------|-----------|--------
`text`  | 纯文本【默认】| `text/plain`
`json`  | JSON      | `application/json`
`xml`   | XML       | `text/xml`
`png`   | PNG图片   | `image/png`
`jpeg`  | JPEG图片  | `image/jpeg`
`bin`   | 二进制    | `application/octet-stream`

根据上述假定，比较适合的场景包括但不限于：

- 微信公众号/小程序
- 社交平台API，譬如微博等
- 云片网等 SMS 服务提供商

--------------------------------------
# 设计思路与边界

首先，本功能实现主体应该是一个服务类 `ThirdXApi`。对于密钥的访问，
配置的读取，接口数据格式化等，都提供扩展接口。

因此它能应用在：

- 原生的 Java 应用，譬如 `Andriod`, `NutzBoot`, `Spring` 等项目
- Walnut 以后可能抽象出来的其他服务类或者Web模块
- Walnut 的 `cmd_xapi` 命令

--------------------------------------
# 类继承图谱

```bash
ThirdXApi          # 抽象接口
#-------------------------------------
# 聚合的接口
|-- ThirdXConifgLoader   # 配置信息加载器
|-- ThirdXAkManager      # 密钥存储管理器
#-------------------------------------
|-- WnThirdXApi    # Walnut 的内置标准实现

```

--------------------------------------
# 使用方式

> 主要是应用在 `cmd_xapi`，这个清参看命令文档 `man xapi`

