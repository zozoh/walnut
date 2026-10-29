---
title: 域应用初始化
module: 101.dev
keywords: [app init, 域初始化, init_name, vars.json, _script, 布局模板, demo.init]
source:
  - ext/data/app/hdl/app_init.java
  - ext/data/app/impl/AppInitService.java
  - ext/data/app/impl/AppInitItemContext.java
  - ext/data/app/bean/init/AppInitItem.java
  - util/explain/WnExplains.java
related:
  - 30-write-app-init.md
  - ../../../src/com/site0/walnut/ext/data/app/man/zh_cn/cmd_app_init.md
---

# 域应用初始化概述

## 什么叫域的初始化

通常来说，一个域会对外提供一个服务，域里所有的数据都在
`/home/{domain}` 目录下管理。因此一个 Walnut 节点在逻辑上可以托管很多个
完全不同逻辑的应用；一个域的应用可大可小，复杂的可以非常复杂。

但不管大小，我们都需要在这个域里构建一系列**标准对象**。譬如：
要做邮件收发，就要提供 `~/.mailx` 这个配置目录；要提供第三方接口服务，
就要有它对应的配置目录；HTTP 注册接口（regapi）、SQL 数据源（`.dao/`）、
Redis 数据源（`.redis/`）等等扩展信息，都要在域里建对应的配置对象。

有些时候，用户的数据也直接存在域里，或者通过域里的一个对象映射到
对应的数据表（IO 映射，见 ../20.io-storage/10-io-mapping.md）。

总之，为了搭建这个域的后端服务，我们需要为域的主目录初始化一系列对象。
如果每一个都手工执行，就过于繁琐了。

## 核心想法：vars.json + 布局模板

大概的想法是：

1. 把域的**关键信息**——包括但不限于邮件 SMTP 的用户名、密码、
   数据库连接串、第三方服务的密钥——全部存储在一个 JSON 文件里，
   通常就是 `~/.domain/vars.json`
2. 再写一个**布局模板**文件（`.init`），逐条声明这个域需要哪些对象、
   各自什么元数据、什么内容，内容里用 `${变量}` 占位
3. 执行一条命令 `app init`，它会自动找到变量文件和布局模板，
   把所有关键信息填充进去，**一口气形成整个域的文件布局**
4. 后续维护中，如果关键信息变了（比如换了数据库密码），直接修改
   `~/.domain/vars.json`，再执行一遍 `app init` 即可

这样做的好处：

- **敏感信息不进代码库**——模板可以放在项目仓库里分发，
  密码密钥只存在于运行环境的对象存储中，不会泄露给第三方
- **一条命令得到一致的布局**——所有人、所有环境跑出来的
  目录结构完全一样，差异只在 vars.json

## 一个真实的例子

以项目 `demo` 的布局模板为例（节选），它描述了一个应用域的完整初始化：

```bash
# 首次开通一个域时，按顺序执行（这段就写在模板文件的头部注释里）：
me -set init_name=demo          # 告诉系统本域用哪个初始化模板
mkdir ~/.domain                  # 域配置目录
cp /mnt/project/demo/init/demo_vars.json ~/.domain/vars.json   # 关键信息
app init                         # 一口气初始化整个域
```

模板文件 `/mnt/project/demo/init/demo.init` 里长这样（节选）：

```text
# 链接项目自带的公共目录
@DIR .gui/  -> /mnt/project/demo/_gui/
@DIR jsbin/ -> /mnt/project/demo/jsbin/

# 敏感信息：S3 密钥从 vars.json 里取，渲染进配置文件
@FILE .io/s3/mail.json5
%TMPL:
{
  "secretId":  "${s3-mail-secret-id}",
  "secretKey": "${s3-mail-secret-key}",
  "region":    "${s3-mail-region}",
  "bucket":    "${s3-bucket-mail}"
}
%END%

# 数据源配置：=jdbc0.url 表示从上下文取 jdbc0.url 的值
@FILE .dao/default.dao.json
%EXPLAIN:
{
  "url" : "=jdbc0.url",
  "username" : "=jdbc0.username",
  "password" : "=jdbc0.password",
  "maxActive": 50
}
%END%

# HTTP 接口：声明一个跨域 JSON API，内容就是它的命令模板
@API .regapi/api/attachment/create 'cross/json'
%COPY:
jsc ~/jsbin/api/create_attachment.js -vars \
  '{html:"id:${id}", tags:"${http-qs-tags?}"}'
%END%

# 最后：给主目录打元数据，设置用户环境变量
@HOME
{title : "${domainName}", icon : "${domainIcon?fas-globe-asia}"}
@ENV
{OPEN : "wn-term", theme : "light", APP_PATH : "/rs/ti/app:/app"}
```

标记语法的完整说明见下文「布局模板的标记语法」；
如何动手编写这样一个模板，见 30-write-app-init.md。

## 命令是如何工作的

`app init`（`app_init.java`）依次做四件事：

1. **找布局模板**（依次尝试，命中即停，见下节）
2. **确定目标目录**：`-dir` 参数，缺省为当前用户的 `~`
3. **找上下文变量**（依次尝试）：`-vars` 参数 → 标准输入 →
   `~/.domain/vars.json`（都没有则为 `{}`）
4. **执行**：解析模板 → 逐条处理条目 → 执行模板目录下的 `_script`
   脚本收尾（可用 `-script off` 关闭；`-script 名称` 换名）

执行过程中逐步输出每个条目的处理日志；完成后输出总耗时。

### 模板的查找顺序

模板统一按 `xxx.init` 命名（推荐约定）。`-by` 参数可换名，非 `.init`
结尾会自动补 `.init` 后缀：

1. 命令第一个参数显式给出的路径：`app init /etc/myapp/init`
2. `~/.domain/init/${init_name}.init`——`init_name` 取用户元数据
   `init_name` 字段，缺省为**用户所在组名**，可用 `INIT_NAME` 环境变量覆盖
3. `/mnt/project/${init_name}/init/domain/${init_name}.init`
4. `/mnt/project/${init_name}/init/${init_name}.init`
5. `/mnt/project/${init_name}/*/init/domain/${init_name}.init`（子目录逐个找）

全部 miss 报错 `e.cmd.app_init.FailToFoundHome`。

实践约定：

- 个人/测试期模板放 `~/.domain/init/`，项目级模板放
  `/mnt/project/<项目名>/init/`
- 给**多个不同用户组**分发不同初始化时，用 `${init_name}.init` 命名区分
  （组名即模板名，demo 就是这样）
- 3~5 级是部署项目模板的位置，通常由运维统一维护

### 常见用法

```bash
app init                          # 初始化自己的 home（自动定位模板）
app init /etc/myapp/init -dir ~/abc   # 指定模板与目标目录
app init -vars '{host:"www.mysite.com"}'   # 显式传变量（优先级高于 vars.json）
echo '{host:"..."}' | app init    # 从管道传入变量
app init -script off              # 只初始化，不执行收尾脚本
```

## 布局模板的标记语法

模板是文本文件（通常叫 `xxx.init`），由一个个 `@` 开头的条目
组成；`#` 开头是注释，空行忽略（解析器 `AppInitService`）。

### 条目首行

```text
@类型 路径 '快捷元数据'{属性JSON} -> 链接目标
```

- **类型**：条目类型，见下文「条目类型」
- **路径**：要创建的对象路径（相对目标目录），支持 `${var}` 占位符
- **快捷元数据**：单引号括起、`/` 分隔的一组速记符号（见下）
- **属性 JSON**：`{}` 括起的元数据；值支持 `${var}` 占位，
  也支持 explain 引用（见下文「值的高级写法」）
- **链接目标**：写 `-> /path/to/target`，给对象设置 `ln` 元数据，
  即对象是指向别处的链接——模板里大量用它把项目目录直接链进域里

首行之后、下一个 `@` 或 `#` 之前的行是**元数据行**，按 JSON 合并进对象元数据
（适合写多行 JSON，或不想挤在一行的情况）：

```text
@DIR www/login/ 'i18n:www-admin-login/<fas-dungeon>' -> /mnt/project/demo/www/login/
{
  session: { path: "~/.domain/session" },
  site: "~/www/login",
  domain: "${domain}"
}
```

### 快捷元数据速记

`'...'` 里的片段用 `/` 分隔，逐个展开为元数据
（`AppInitItem.addQuickMeta`）：

- `=media` → `tp:"media"`
- `<fas-xxx>` → `icon:"fas-xxx"`（`<zmdi-xxx>` 等同理，取的就是引号内容）
- `cross` → `http-cross-origin:"*"`
- `json` → `http-header-Content-Type:"text/json"`
- `dynamic` → `http-dynamic-header:true`
- `hook` → `run-with-hook:true`
- `http302` → `http-resp-code:302`
- `auth` → 一组 `http-www-*` 站点访问控制元数据
- `i18n:xxx` 或其他任意字符串 → `title:"该字符串"`
  （界面按 title 显示名称，所以 demo.init 里大量 `'i18n:history-record/<zmdi-time-restore>'`
  这样的写法：标题 + 图标）

### 内容行

FILE/API 条目可以有内容，两种来源形式（`AppInitService._P1`）：

```text
[%|?](COPY|TMPL|EXPLAIN){内容变量} > 模板目录下的内容文件
[%|?](COPY|TMPL|EXPLAIN){内容变量}
多行内容……
%END%
```

- 行首 **`%` 强制覆盖**（目标文件已有内容也重写）；**`?` 只在文件为空时
  写入**——默认不覆盖已有文件，重跑 `app init` 是安全的
- `> 文件路径`：内容取自**布局模板所在目录**下的那个文件
  （`%COPY>` 可原样复制二进制，如 logo 图片）
- 内联形式的内容一直读到 `%END%` 行为止——**别忘了写 `%END%`**，
  否则后面的条目会被吞进内容里
- 三种内容模式：
  - **COPY**：原样写入，不做任何渲染
  - **TMPL**：内容作为 `WnTmpl` 模板，用上下文变量渲染后写入，
    `${var}` / `${var?默认值}` 占位
  - **EXPLAIN**：内容作为 JSON，按 `WnExplain` 规则解释后美化写入，
    适合配置文件（值的写法见下）

### 值的高级写法（explain 引用）

元数据和 EXPLAIN 内容里的字符串值，以特定前缀开头会被解释
（`WnExplains`，被 `Wn.explainObj` 统一处理）：

- `=key` → 从上下文取 `key` 的值；`=..` 取整个上下文
- `=key?默认值` → 取不到时用默认值（默认值以 `=` 开头会转成 Java 值，
  如 `=50` 是数字）
- `==key` / `!=key` → 取布尔判断结果
- `=>表达式` → EL 表达式求值
- `->模板` → 按模板渲染；`key??模板` 仅在 key 非空时渲染
- 以 `:` 开头转义：`::=xx` 输出字面量 `=xx`，
  `:-` `:` `:!` 等同理

demo.init 里的典型用法：

```text
# 数据库密码不进模板文件，从 vars.json 的 jdbc0.password 取
"password" : "=jdbc0.password"

# 整个值就是上下文的 server_config 变量（一个 JSON 对象）
%EXPLAIN:
"=server_config"
%END%

# 元数据引用变量（=debugApp 取 vars.json 里的 debugApp）
@DIR .domain/ 'i18n:sys-settings/<fas-cogs>'
{"debug-app-wn-manager":"=debugApp"}
```

### 条目类型

`@` 后的类型决定这条声明做什么（处理器映射在 `AppInitContext`）：

- `@DIR`：建目录（没有则创建），写入元数据
- `@FILE`：建文件，写入元数据；有内容行则写内容
- `@API`：与 `@FILE` 类似，但属性里的 `cross` / `mime` / `hook` /
  `dynamic` 会被转译成对应的 `http-*` 元数据——专门用来声明 regapi
  接口文件（接口机制见 ../50.regapi/README.md）
- `@HOME`：把元数据写到目标目录（`oDist`）本身，
  常用于给 home 打业务标记（域名、图标）
- `@ENV`：元数据的每个键值对转成 `me -set 'key=val'` 执行，
  即设置当前用户的环境变量
- `@INCLUDE`：引入模板目录下的另一个模板文件，以本条目的元数据为
  补充变量在子作用域执行（输出带 `>>` 前缀，便于看出嵌套关系）

### 收尾脚本

模板执行完后，命令在模板所在目录找 `_script` 文件（`-script` 换名，
`-script off` 关闭），读出来用 `WnTmpl` 渲染一遍再交给命令解释器执行。
适合放「模板干不了的活」：发通知、跑 `sqlx @exec` 建表、
`jsc` 执行一段初始化逻辑等。

## 与其他机制的关系

- 初始化的对象是**标准对象**（目录、文件、元数据），字段体系见
  ../10.concepts/01-std-obj.md
- `${var}` 占位与 `%date:now` 等文本宏，与 sqlx 模板共用同一套
  `WnTmplX` 引擎（见 21-write-sqlx-tmpl.md 开头「占位符」一节）
- `-> 链接目标` 大量用于把项目目录链进域里；链接是对象的 `ln` 元数据

# 延伸阅读

- 30-write-app-init.md —— 模板编写实践：以真实模板为线索讲解组织方式与常用套路
- cmd_app_init man 文档（参数权威说明）：
  ../../../src/com/site0/walnut/ext/data/app/man/zh_cn/cmd_app_init.md
- 源码：src/com/site0/walnut/ext/data/app/hdl/app_init.java、
  src/com/site0/walnut/ext/data/app/impl/AppInitService.java
