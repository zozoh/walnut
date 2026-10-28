---
title: 编写应用初始化模板（app init）
module: 101.dev
keywords: [app init, 应用初始化, .init, AppInitService, 域初始化, vars.json]
source:
  - ext/data/app/hdl/app_init.java
  - ext/data/app/impl/AppInitService.java
  - ext/data/app/impl/AppInitItemContext.java
  - ext/data/app/bean/init/AppInitItem.java
related:
  - ../../src/com/site0/walnut/ext/data/app/man/zh_cn/cmd_app_init.md
---

# 编写应用初始化模板概述

一个业务系统给新用户（域）开通时，总要干几件重复的事：
建好目录骨架、写好配置文件、设置元数据、初始化环境变量。
`app init` 把这件事做成了**声明式模板**：你写一个 `.init` 文本文件，
逐条声明「创建什么、什么属性、什么内容」，命令逐条执行，
一次把一个用户的 home 初始化成业务需要的样子。

```bash
# 按模板初始化自己的主目录
app init /etc/myapp/init

# 初始化到指定目录，并注入占位符变量
app init /etc/myapp/init -dir ~/abc -vars '{host:"www.mysite.com"}'
```

命令本身的参数参见 `cmd_app_init` 的 man 文档，本篇只讲**模板怎么写**。

## 命令是如何找模板和变量的

`app init` 的执行分四步（`app_init.java`）：

1. **找模板文件**（依次尝试，找到即停）：
   - 命令第一个参数指定的路径下的 `_files`（或 `-by` 指定的文件名）
   - `~/.domain/init/_files`
   - `~/.domain/init/${init_name}.init`（`init_name` 取用户元数据 `init_name`，缺省为用户的组名）
   - `/mnt/project/${init_name}/init/domain/_files`
   - `/mnt/project/${init_name}/init/_files`
   - `/mnt/project/${init_name}/init/${init_name}.init`
   - `/mnt/project/${init_name}/*/init/domain/_files`（子目录逐个找）
2. **找目标目录**：`-dir` 参数，缺省为 `~`
3. **找上下文变量**（依次尝试）：
   - `-vars` 参数给出的 JSON
   - 标准输入（管道传入）
   - `~/.domain/vars.json`（没有则为 `{}`）
4. **执行与收尾**：解析模板 → 逐条处理 → 执行同目录下的 `_script` 脚本
   （可用 `-script off` 关闭）

## 模板文件的结构

模板就是一个文本文件（通常叫 `_files` 或 `xxx.init`），由一个个 `@` 开头的条目组成。
`#` 开头是注释，空行忽略（`AppInitService.parse`）：

```text
@DIR www '我的站点' =media <fas-globe>
@FILE www/index.html '首页' =html
  tp: html
  http-www-home: ~/www
?TMPL> index.html
@ENV
  MY_APP_MODE: product
```

每个条目的首行格式为（正则见 `AppInitService.REGEX`）：

```text
@类型 路径 '快捷元数据'{属性JSON} -> 链接目标
```

- **类型**：条目类型，见下一节
- **路径**：要创建的对象路径（相对目标目录），支持 `${var}` 占位符
- **快捷元数据**：单引号括起，`/` 分隔的一组速记符号（见下）
- **属性 JSON**：`{}` 括起的元数据，同样支持占位符
- **链接目标**：写 `-> /path/to/target` 表示给对象设置 `ln` 元数据

首行之后、下一个 `@` 或 `#` 之前的行是**元数据行**，按 JSON 合并进对象元数据
（适合写多行 JSON 或不想挤在一行的情况）。

如果其中某一行匹配 `%TMPL{...}> 文件路径` 这样的内容行，则它后面的内容
（或 `> 文件路径` 指向的模板文件）成为条目的**内容**。

### 快捷元数据速记

写在 `'...'` 里的片段，逐个展开为元数据（`AppInitItem.addQuickMeta`）：

- `=media` → `tp:"media"`
- `<fas-xxx>` → `icon:"fas-xxx"`
- `cross` → `http-cross-origin:"*"`
- `json` → `http-header-Content-Type:"text/json"`
- `dynamic` → `http-dynamic-header:true`
- `hook` → `run-with-hook:true`
- `http302` → `http-resp-code:302`
- `auth` → 一组 `http-www-*` 访问控制元数据
- 其他任意字符串 → `title:"该字符串"`

## 条目类型

`@` 后面的类型决定这条声明做什么（处理器映射在 `AppInitContext`）：

- `@DIR`：建目录（没有则创建），写入元数据
- `@FILE`：建文件，写入元数据；如有内容行则写内容
- `@API`：与 `@FILE` 类似，但属性里的 `cross`/`mime`/`hook`/`dynamic`
  会被转译成对应的 `http-*` 元数据，专门用来声明 regapi 接口文件
- `@HOME`：把元数据写到目标目录本身（`oDist`），常用于给 home 打业务标记
- `@ENV`：把元数据的每个键值对转成 `me -set 'key=val'` 命令执行，
  即设置当前用户的环境变量
- `@INCLUDE`：引入同目录下的另一个模板文件，用本条目的元数据
  作为补充变量在其子作用域执行（输出带 `>>` 前缀，便于看出嵌套关系）

条目的元数据在执行前会统一过一遍 `Wn.explainObj(vars, meta)`，
也就是所有 `${var}` 都会被上下文变量替换。

## 条目内容

内容行的格式（`AppInitService._P1`）：

```text
[%|?](COPY|TMPL|EXPLAIN){内容变量} > 同目录内容文件
[%|?](COPY|TMPL|EXPLAIN){内容变量}
多行内容……
%END%
```

- 行首 `%` 表示**强制覆盖**：目标文件已有内容也重写；
  `?` 表示只在文件为空（空 SHA1）时写入——**默认不覆盖已有文件**
- `> 文件路径` 形式：内容取自**模板所在目录**下的那个文件（支持二进制复制）；
  内联形式的内容一直读到 `%END%` 行为止，**别忘了写 `%END%`**，
  否则后面的条目会被吞进内容里
- **COPY**：原样写入（`>` 形式支持二进制复制），内容里的占位符不处理
- **TMPL**：内容作为 `WnTmpl` 模板，用上下文变量渲染后写入
  （上下文 = 命令级 vars 叠加 `{...}` 里的内容变量）
- **EXPLAIN**：内容作为 JSON，先 `WnExplain` 解释（含占位符），再美化输出写入，
  适合生成结构化的配置 JSON

```text
@FILE www/config.json '站点配置'
?EXPLAIN{
  "host": "${host}",
  "createdAt": "%ms:now"
}
```

## 一个完整的例子

模板 `/etc/myapp/init/_files`：

```text
# 初始化 myapp 的域
@HOME
  myapp_ver: "1.0"

@DIR www '<fas-globe>' =media

@FILE www/index.html '首页' =html
  tp: html
?TMPL{
  title: "你好 ${nickname?friend}"
}
<h1>Welcome to ${host}</h1>
%END%

@API www/api/ping '心跳' 'cross/json'
%TMPL> ping.json

@ENV
  MYAPP_HOME: "~/www"
```

其中 `ping.json` 是模板目录里与 `_files` 同级的文件：

```json
{
  "code": 0,
  "data": "pong"
}
```

```bash
# 为新用户执行初始化
app init /etc/myapp/init -vars '{host:"www.mysite.com", nickname:"小明"}'
```

执行后：home 目录打上 `myapp_ver`；`~/www` 建成媒体目录；
`~/www/index.html` 渲染模板写入；`~/www/api/ping` 成为跨域 JSON 接口；
当前用户环境变量多了 `MYAPP_HOME`。

## 收尾脚本

模板执行完后，命令会在模板所在目录找 `_script` 文件（可用 `-script` 换名，
`-script off` 关闭），读出来用 `WnTmpl` 渲染一遍再交给命令解释器执行。
适合放「模板干不了的活」：发个通知、跑一次 `sqlx @exec` 建表、
调用其他命令完成联动初始化。

# 延伸阅读

- `app init` 命令参数与查找顺序的权威说明：
  `src/com/site0/walnut/ext/data/app/man/zh_cn/cmd_app_init.md`
- 同组命令：`cmd_app.md`（应用目录结构约定）、`cmd_app_setup.md`、
  `cmd_app_i18n.md`（i18n 相关）
- 模板占位符机制：../11-write-sqlx-tmpl.md 开头的「占位符」一节
  （`WnTmpl` 与 sqlx 模板共用同一套引擎）
- 模板解析实现：src/com/site0/walnut/ext/data/app/impl/AppInitService.java
