---
title: 人机验证码 captcha
module: security.captcha
keywords: [验证码, captcha, 人机校验, 拖拽验证, WnCaptchaApi, WnCaptchaObj]
source:
  - security/captcha/
related: [README, 60-auth-otp]
---

# 人机验证码 captcha 概述

**人机验证码 captcha** 干一件事：区分人和机器。批量注册、撞库、刷短信的
机器人，必须在敏感操作前被拦下来。OTP 负责「确认账号归属」，captcha 负责
「挡机器人」，两者独立成服务、由业务编排串联（协作流程见本文最后一节，
OTP 本身见 60-auth-otp）。

captcha 默认采用**拖拽式**：服务端生成一道验证题，序列化成字符串交给客户端，
由客户端绘制出拖拽界面。它也不依赖会话——验证发生时，往往还没登录呢。

## 要解决的问题

一个对外开放注册的 Walnut，会立刻面对这个麻烦：

- **机器人**：批量注册、撞库、刷短信——不拦下来，短信账单先爆，
  数据库里也会灌满假账号。敏感操作前必须有一道**人机校验**，
  这就是 captcha 存在的意义。

## 解决思路

captcha 单独一套服务类，放在 `security/captcha` 包里。
现在只定**接口骨架**，拖拽题怎么生成、答案怎么判定，这些细节晚点再设计。
接口就三个角色：

```bash
|-- WnCaptchaBuilder 按配置名构建
|-- WnCaptchaApi 生成/校验验证码
|-- WnCaptchaObj 验证码的序列化载体
配置：~/.captcha/{name}.json
```

# Captcha 服务

## 接口三角色

- **`WnCaptchaBuilder`**：按配置名构建验证码接口，相当于工厂——
  `builder.build("default")` 拿到一个 `WnCaptchaApi`
- **`WnCaptchaApi`**：验证码服务的统一入口，提供**生成**与**校验**两个能力
- **`WnCaptchaObj`**：一份验证码的载体，`toString()` 序列化成字符串交给客户端，
  `fromString()` 从字符串还原——客户端拿到什么、回传什么，全靠它定义

## 配置：`~/.captcha/`

配置放在 root 家目录的 `~/.captcha/`，一个文件就是一种验证码方式：

```bash
~/.captcha/
|-- default.json     # 拖拽式验证码的配置，包括图片策略等配置信息
|-- ...              # 我们以后还可以支持更多验证码方式
|-- ...              # 譬如选择汉字，计算数学题的，等等
```

## 生成与校验的分工

一次完整的验证码交互（生成 + 校验）是这样分工的：

```bash
服务器：生成部分
|-- WnCaptchaBuilder.build("default") 获取一个 WnCaptchaApi
|-- WnCaptchaApi 生成验证码对象 WnCaptchaObj
|-- WnCaptchaObj 的 toString() 将自己变成字符串
|-- 服务器将字符串返回给客户端

客户端
|-- 根据服务器返回的字符串，绘制验证码界面
|-- 接收用户输入，保留信息

服务器：校验部分
|-- WnCaptchaBuilder.build("default") 获取一个 WnCaptchaApi
|-- WnCaptchaApi 校验用户输入，返回校验结果
```

**注意一个留白**：验证码的状态（题目、答案、有效期）存在哪、怎么保证
「一发一验」，属于「细节后补」的部分——但 `WnCaptchaObj` 的序列化格式
必须能承载这些信息，因为它是客户端与服务器的唯一契约。

# OTP 与 captcha 的协作

两个服务各自独立，靠业务编排串联。以「OTP 登录」场景为例，全流程如下：

```bash
客户端
|-- 展示登录界面：用户名、密码、OTP 输入框，旁边一个「获取 OTP」按钮

第 1 轮：获取 OTP 之前，先过人机校验
|-- 用户点击「获取 OTP」，向服务器请求 captcha
服务器
|-- 进入 captcha 生成流程，返回 captcha 字符串
客户端
|-- 根据字符串绘制拖拽验证码界面
|-- 用户完成拖拽，点击登录

第 2 轮：登录提交
|-- 客户端将用户名、密码、验证码（拖拽结果）一起发送给服务器
服务器
|-- 进入 captcha 校验流程，先校验验证码
|-- 校验失败 → 直接返回失败（不碰用户名密码）
|-- 校验通过 → 继续验证用户名、密码
|-- 用户名密码不通过 → 返回登录失败
|-- 全部通过 → 触发 OTP 发送（WnOTPSender），等待用户回填
```

OTP 本身的发送时机由具体业务决定（登录场景里，用户名密码通过后再发码，
可以避免「验证码被用来探测账号是否存在」）。

# 主要代码类

- `WnCaptchaBuilder` — captcha 工厂接口，`build(name)` 按 `~/.captcha/{name}.json` 构建
- `WnCaptchaApi` — captcha 服务统一接口：生成与校验（当前为空骨架）
- `WnCaptchaObj` — 验证码载体：`toString()` 序列化给客户端，`fromString()` 还原

# 扩展阅读

- 60-auth-otp — 动态密码 OTP 服务：生成、存储（Redis）、邮件发送的完整机制
