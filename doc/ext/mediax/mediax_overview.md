---
title  : MediaX概述
author : zozoh
tags:
- 扩展
- mediax
---

# MediaX 是一个工具类库

Mediax 是一个工具类库，可以用它来对接其他的媒体资源，譬如电商平台，自媒体平台，论坛，甚至电子邮件或者短信。

- 本工具类被假想在 `JvmBox` 环境下运行，因此用户信息对它是透明的
- `WnIo` 接口会根据 `WnContext` 保证数据的安全性

整个类库分作下面几个部分

1. `MediaXAPI` 接口抽象实现：封装了主要的逻辑
2. `MediaXService` 一个抽象工厂，负责创建 `MediaxAPI` 实例
    - `WnMeidaXService` 是 walnut 版实现
    - 该实现将登录信息存放在某个文件夹下
3. `cmd_mediax` 是对 `WnMediaXService` 的封装，用来测试或者单独执行

# MediaX 的抽象动作

由于 MediaX 是一个抽象的库，它假想你会对某个指定的媒体平台做相同逻辑的操作：

1. 提交 `post`，譬如自媒体发稿，论坛发帖，等行为，你会投递图文混合的素材
2. 爬取 `crawal`，相当于浏览列表，得到数据的概要信息等
3. 获取 `fetch`，得到某个指定资源尽可能详细的信息，即一个 JSON 对象
4. 下载 `download`，某个指定资源，譬如一个PDF，视频，ZIP 包等

为了保证抽象性，譬如，你想从 http://icp.chinaz.com/ 爬取一批北京地区的最新备案信息

```bash
mediax crawal http://icp.chinaz.com/provinces?prov=京&domain=10
```

我们支持你这么写

```
mediax crawal http://icp.chinaz.com 最新备案/京
```

每个接口都支持一些这样抽象指定资源的方式，可以执行帮助命令，了解每个媒体平台我们支持的快捷目标

```bash
mediax target http://icp.chinaz.com
crawl:
    最新备案/${prov}/${page} : 最新备案/京/3
fetch:
    --
```

同时，为了更容易测试，支持输出测试 URL 的方法

```bash
mediax explain crawl http://icp.chinaz.com 最新备案域名/京
http://icp.chinaz.com/provinces?prov=京&domain=10      # <- 将会输出真实URL
```


# MediaX 的工作流程

```java
// 得到工厂类实例
MediaXService mx = ...

// 创建接口
MediaXAPI api = mx.create("www.toutiao.com", null);

// 执行发送
// 抽象类 AbstractMediaXAPI 提供了 checkTicket 方法
// 发送前需要登录的子类，调用这个方法，取回自己的连接票据
// 这个函数会保证票据只会被生成一次
MxPost obj = ...
MxRePost re = api.post(obj);

// 搜刮最多100条数据
MxCrawl cr = new MxCrawl().uri("http://xxxxx?id=xxx").limit(100);
List<MxReCrawl> list = api.crawl(cr);

// 搜刮在某个指定日期以后全部数据
MxCrawl cr = new MxCrawl().uri("http://xxxxx?id=xxx").lastDate("2018-03-22");
List<MxReCrawl> list = api.crawl(cr);

// 搜刮在某个指定日期以后最多 100 条数据
MxCrawl cr = new MxCrawl().uri("http://xxxxx?id=xxx")
                            .lastDate("2018-03-22").limit(100);
List<MxReCrawl> list = api.crawl(cr);
```

# `WnMediaXService` 目录结构

你需要提供一个目录给 `WnMediaXService`，当然它必须是当前线程有权限访问的。
默认的，本类将认为它是 `~/.mediax`

```bash
~/.mediax
    www.toutiao.com/    # 每个网站一个目录
        zozoh           # 每个账号一个文件
        胖五             # 中文也没有问题
        wendal          # 有的网站可能不需要登录
```

文件的内容JSON：

```js
// 验证用的账号密码，当然对于今日头条，微信公众号，这个相当于是
// AppID/SecretKey
login  : "xxxx"
passwd : "xxxx"
token  : "xxxx"     // 有些平台（譬如微信公众号）需要这个

// 下面的信息对于查找比较有帮助
nickname : "xiaobai"      // 一个对应平台上的昵称
alvl     : 5              // 账号级别 1-5，表示在平台的级别1是新手，5是顶级
avatar   : "http://xxx"   // 存该平台上的头像

// TODO ... 想到再加咯 ...
```

# ApiKey

每个API的实现类都存放在 `org.nutz.walnut.ext.mediax.apis` 包下，每个实现类都要声明注解:

```java
@MxAPIKey("icp.chinaz.com")
public class ChinaZMeidaXAPI extends NoTicketMediaXAPI {
    ...
}
```

这个 Key 是根据用户传入的 URI 来生成的，规则是：

- 如果是http/https 协议看 host
- 否则用协议名

譬如：

```bash
https://www.toutiao.com/a6534824108553667085/
# apiKey = www.toutiao.com

mailto:xiaobai@126.com
# apiKey = mailto
```


# 媒体平台列表

- 阿里巴巴
- ChinaZ
- Discuz 论坛
- 淘宝
- 58同城
- 天涯论坛
- 简书
- 今日头条
- QQ群
- 新浪微博
- 悟空问答
- 微信公众号
- 网易号
- 知乎
- 知乎专栏
- 电子邮箱
- 手机短信























