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


# `MediaX` 的工作流程

```java
// 得到工厂类实例
MediaXService mx = ...

// 创建接口
MediaXAPI api = mx.create("www.toutiao.com");

// 准备发送数据
MxPost obj = ...

// 执行发送
// 抽象类 AbstractMediaXAPI 提供了 checkTicket 方法
// 发送前需要登录的子类，调用这个方法，取回自己的连接票据
// 这个函数会保证票据只会被生成一次
MxRePost re = api.post(obj);

// 搜刮最多100条数据
List<MxReCrawl> list = api.crawl("http://xxxxx?id=xxx", 100);

// 搜刮在某个指定日期以后全部数据
List<MxReCrawl> list = api.crawl("http://xxxxx?id=xxx", "2018-03-22");

// 搜刮在某个指定日期以后最多 100 条数据
List<MxReCrawl> list = api.crawl("http://xxxxx?id=xxx", "2018-03-22", 100);
```

# `WnMediaXService` 目录结构

你需要提供一个目录给 `WnMediaXService`，当然它必须是当前线程有权限访问的。
默认的，本类将认为它是 `~/.mediax`

```bash
~/.mediax
    toutiao_zozoh       # 这个名字可以随便起
    toutiao_pangwu      # 文件名会当做Key
    今日头条_wendal       # 中文也没有问题
```

文件的内容为空，必要的信息全部存在元数据上：

```js
// 验证用的账号密码，当然对于今日头条，微信公众号，这个相当于是
// AppID/SecretKey
login  : "xxxx"
passwd : "xxxx"
token  : "xxxx"     // 有些平台（譬如微信公众号）需要这个

// 下面的信息对于查找比较有帮助
nickname : "xiaobai"   // 一个对应平台上的昵称
alvl     : 5           // 账号级别 1-5，表示在平台的级别1是新手，5是顶级
thumb    : "id:xxx"    // 存该平台上的头像

// TODO ... 想到再加咯 ...
```

# 媒体平台的类型列表

键         | 说明
-----------|--------------------
ALI        | 阿里巴巴
CHINAZ     | ChinaZ
DISCUZ     | Discuz 论坛
TB         | 淘宝
TC58       | 58同城
TIANYA     | 天涯论坛
JIANSHU    | 简书
JRTT       | 今日头条
QUN        | QQ群
WEIBO      | 新浪微博
WKWD       | 悟空问答
WXMP       | 微信公众号
WYHAO      | 网易号
ZHIHU      | 知乎
ZHZL       | 知乎专栏
EMAIL      | 电子邮箱
SMS        | 手机短信























