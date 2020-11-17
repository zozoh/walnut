---
title: 域应用初始化
author: zozohtnt@gmail.com
key: c2-init
---

# 模板目录的结构如下:

```bash
/path/to/dir/
|-- _files          # 初始化配置文件
|-- _script         # 最后要执行的初始化脚本
|-- abc.png         # 在 _files 里面指定的文件内容
|-- xyz.txt         # 就是个纯文本
```

--------------------------------------
# 初始化配置文件

配置文件有下面几种段：

 Name      | 描述
-----------|---------------
`@DIR`     | 目录
`@FILE`    | 文件
`@THING`   | 数据集
`@API`     | 接口文件
`@ENV`     | 环境变量
`@HOME`    | 主目录
`@INCLUDE` | 引入另外的初始化文件


--------------------------------------
## `@DIR`目录

```bash
@DIR .ti/ -> /mnt/project/${domain}/admin/_ti/
{
  ...
}
```

--------------------------------------
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

--------------------------------------
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

--------------------------------------
## `@API`接口

```bash
@API{cross:true,mime:"text/json"} auth/login_by_wxcode
%COPY> /mnt/prject/${domain}/regapi/auth/login_by_wxcode
```

--------------------------------------
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

--------------------------------------
## `@HOME`主目录

```bash
@HOME
{
  icon:"fas-globe-asia", 
  title:"OnChina"
}
```

--------------------------------------
## `@INCLUDE`引入

```bash
@INCLUDE /etc/init/include/api/auth
{
  domain: "${domain}"
}
```

> 元数据会覆盖老的上下文，值域仅为引入的文件

--------------------------------------
# 文件内容宏

对于 `@FILE` 和 `@API` 两种段，支持初始化文件内容。
这个初始化操作由所谓的 `文件内容宏` 来完成，下面是我们支持的宏：

Macro    | Force | Description
---------|-------|-------
`%COPY:` | Yes   | 从下一行开始，是内容, 直到遇到宏 `%END%`
`?COPY:` | No    | 从下一行开始，是内容, 直到遇到宏 `%END%`
`%TMPL:` | Yes   | 从下一行开始，是模板, 直到遇到宏 `%END%`
`?TMPL:` | No    | 从下一行开始，是模板, 直到遇到宏 `%END%`
`%COPY>` | Yes   | 当前行为文件路径，指向内容
`?COPY>` | No    | 当前行为文件路径，指向内容
`%TMPL>` | Yes   | 当前行为文件路径，指向模板
`?TMPL>` | No    | 当前行为文件路径，指向模板

---------------
>下面是一些例子:

```bash
# copy 一个文件的内容
%COPY> xyz.txt

# 读取文件内容，进行转换，并写入目标
%TMPL> xyz.txt

# 直接声明了文本内容，遇到文件结尾结束，或者 %END% 行为结尾
# 内容会被 trim
%COPY: hello world
this is second line
%END%

# 直接声明了文本内容，就这一行
%TMPL% hello ${name}
```  

--------------------------------------
# 初始化上下文

由 `app init` 命令从参数里获得