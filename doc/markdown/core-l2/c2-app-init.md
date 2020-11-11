---
title: 域初始化命令
author: zozohtnt@gmail.com
---

--------------------------------------
# 初始化配置文件

配置文件有下面几种段：

 Name    | 描述
---------|---------------
`@DIR`   | 目录
`@FILE`  | 文件
`@THING` | 数据集
`@API`   | 接口文件
`@ENV`   | 环境变量
`@HOME`  | 主目录

## `@DIR`目录

```bash
@DIR .ti/ -> /mnt/project/${domain}/admin/_ti/
{
  ...
}
```

## `@FILE`文件

```bash
@FILE .domain/payment/after
{
  ...
}
%COPY:
www payafter ${or_id} -site ~/www/onchina -basket 
%END%
```

## `@THING`数据集

```bash
# 会自动展开目录目录里的 thing-xxx.json，作为链接
# 如果没有 thing.json，则会通过 thing init 初始化一个
# 默认会增加一个 th_auto_select:true 除非再元数据段明确声明为 false
@THING{title:'i18n:oc-tour-posts', icon:'fab-pagelines'} accounts/ -> /mnt/project/${domain}/thing/account/
{
  ...
}
```

## `@API`目录段

> 与 `@FILE` 相同，只是做一个标记

## `@ENV`环境变量

```bash
@ENV
{
  "OPEN" : "wn.manager",
  "THEME" : "light",
  "APP_PATH" : "/rs/ti/app:/app",
  "VIEW_PATH" : "/mnt/project/onchina/admin/view/:/rs/ti/view/",
  "SIDEBAR_PATH" : "~/.ti/sidebar.json:/rs/ti/view/sidebar.json"
}
```

## `@HOME`主目录

```bash
@HOME
{
  icon:"fas-globe-asia", 
  title:"OnChina"
}
```


--------------------------------------
# 初始化上下文

由 `app init` 命令从参数里获得