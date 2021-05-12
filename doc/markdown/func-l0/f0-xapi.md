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

 Name       | Title     | Mime
------------|-----------|--------
`form`      | 普通表单[【默认】 | `application/x-www-form-urlencoded`
`multipart` | 文件流表单 | `multipart/form-data`
`json`      | JSON      | `application/json`
`xml`       | XML       | `text/xml`
`text`      | 纯文本    | `text/plain`
`bin`       | 二进制    | `application/octet-stream`

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
## 类继承图谱

```bash
# 
# 核心业务逻辑是由一个抽象类完成的
# 各个子类，不过是为这个抽象类提供
#  experts/configs
# 的具体实现
#
AbstractThirdXApi implements ThirdXApi
#-------------------------------------
# 封装了各个第三方接口的行为差异
|-- experts : ThirdXExpertManager
#-------------------------------------
# 封装了配置信息加载方式的差异
|-- configs : ThirdXConfigManager
```

--------------------------------------
## 调用顺序

```java
// 1. 准备 API 实例
ThirdXApi api = new WnThirdXApi(sys);

// 2. 获取请求对象
ThirdXRequest req = api.prepare(apiName, account, path, vars);

// 3. 解析请求参数
req.explainHeaders(vars);
req.explainParams(vars);

// 4. 发送请求
InputStream ins = api.send(req, InputStream.class);

// 5. 处理响应流
sys.out.writeAndClose(ins);
```

--------------------------------------
## 配置方式

实际上整个 `XAPI` 支持多少种平台，是根据配置获得的。
即，针对不同平台访问的方式由一个 JSON 配置文件来管理。
下面我们拿 `weixin` 公众号接口来举例：


```js
//
// 下面这个 JSON 会被加载为一个 ThirdXExpert
//
{
  // 接口的公共起始路径
  "base" : "https://api.weixin.qq.com/",
  // 调用超时(毫秒)
  "timeout" : 3000,
  // 连接超时(毫秒)
  "connectTimeout" : 1000,
  // 配置信息的主目录
  "home" : "~/.weixin",
  // 配置文件路径，这个文件被认为是一个 JSON 对象，配置了一组名值对
  "configFilePath" : "wxconf",
  // 存储访问密钥的文件名。这个文件存储在 `${home}/${account}/` 路径下
  // 譬如 `~/.weixin/demo/`
  "accessKeyFilePath" : "access_token",
  //【选】动态密钥的获取路径
  // 如果声明了这个，则表示访问密码是动态获取的
  // 参数模板的上下文就是配置文件
  "accessKeyRequest" : {
    "path"   : "cgi-bin/token",
    "method" : "GET",
    "params" : {
      "appid" : "=appID",
      "secret" : "=appsecret",
      "grant_type" : "=grant_type?client_credential"
    },
    "dataType" : "json"
  },
  // 每次生成密钥文件对象设置的元数据
  // 如果是动态请求，那么这个映射表就是如何从响应里取值
  // 否则，就是如何从配置文件中取值
  // 无论怎样，都需要下面三个字段
  "accessKeyObj" : {
    "ticket"   : "=access_token",  // 访问密钥的值
    "expiTime" : "=expires_in?7200", // 过期时间
    "expiTimeUnit" : "s"  // 过期时间的单位 (s|m|h|d|w)
  },
  //
  // 当前的接口支持下面这些请求
  //  - 键为请求的路径（会自动拼合base）
  //  - 值为一个请求对象
  //
  // 上下文中，有一个固定的值 @AK 表示获取的 API 的票据值
  "requests" : {
      "gh_user_info" : {
        "path" : "cgi-bin/user/info",
        "method" : "GET",
        "headers" : {},
        "params" : {
          "access_token" : "=@AK",
          "openid" : "=openid",
          "lang" : "=lang?zh_CN"
        },
        "bodyType" : "form",
        "dataType" : "json"
      },
      "wxacode_get_unlimited" : {
        "path" : "wxa/getwxacodeunlimit",
        "method" : "POST",
        "headers" : {},
        "params" : {
          "access_token" : "=@AK"
        },
        "bodyType" : "json",
        "body" : {
          "scene" : "=scene",
          "page" : "=page",
          "width" : "=width",
          "auto_color" : "=auto_color",
          "line_color" : "=line_color",
          "is_hyaline" : "=is_hyaline"  
        },
        "dataType" : "jpeg",
        "acceptHeader" : {
            "Content-Type" : "image/jpeg"
        }
      }
    }
}
```

--------------------------------------
# 使用方式

> 主要是应用在 `cmd_xapi`，这个清参看命令文档 `man xapi`

