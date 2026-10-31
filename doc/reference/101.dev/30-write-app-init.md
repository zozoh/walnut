---
title: 编写应用初始化模板
module: 101.dev
keywords: [app init, 初始化模板, 编写实践, vars.json, demo.init, 敏感信息]
source:
  - ext/data/app/impl/AppInitService.java
  - ext/data/app/impl/AppInitItemContext.java
  - ext/data/app/bean/init/AppInitItem.java
related:
  - 10-init-domain-app.md
  - ../../../src/com/site0/walnut/ext/data/app/man/zh_cn/cmd_app_init.md
---

# 编写应用初始化模板概述

布局模板的**标记语法**（条目首行、快捷元数据、内容行、explain 引用、
条目类型）在 10-init-domain-app.md 有完整说明，本篇不再重复。
这里讲的是**实践**：拿到一个真实业务，怎么组织一个初始化模板，
以及编写时的约定和坑。

## 模板的典型结构

以真实项目 `demo` 的 `demo.init`（约 470 行）为例，一个域的布局模板
通常按这样的顺序组织：

1. **头部注释：开通手册**。首次开通一个域要敲的命令直接写在文件头注释里，
   运维照着抄就行：

   ```text
   # 初始化文件:
   # me -set init_name=demo
   # mkdir ~/.domain
   # cp /mnt/project/demo/init/demo_vars.json ~/.domain/vars.json
   # app init
   ```

2. **链接项目公共目录**。用 `-> 链接目标` 把项目仓库里的目录直接链进域：
   脚本、界面组件、SQL 模板这些「跟着代码库走」的东西不该复制进用户目录。

   ```text
   @DIR .gui/   -> /mnt/project/demo/_gui/
   @DIR jsbin/  -> /mnt/project/demo/jsbin/
   ```

3. **引用型配置文件**（lookup 表、字典、报文模板）：内容跟着项目仓库走，
   用 `%EXPLAIN>` / `%COPY>` 从模板目录读内容；**只读**的用 `?`，
   需要每次刷新的用 `%`。

   ```text
   @FILE .lookup/bp.lookup.json
   %EXPLAIN> lookup/bp.lookup.json
   ```

4. **敏感信息配置**（S3 密钥、第三方 API key）：内容用 `%TMPL` 内联，
   值全部来自 vars.json 的 `${变量}`——模板可以放心进代码库：

   ```text
   @FILE .io/s3/mail.json5
   %TMPL:
   {
     "secretId":  "${s3-mail-secret-id}",
     "secretKey": "${s3-mail-secret-key}",
     "region":    "${s3-mail-region}"
   }
   %END%
   ```

5. **数据源配置**（`.dao/`、`.redis/`）：用 `%EXPLAIN` + `=key` 引用
   把连接串、密码从 vars.json 取出来——密码永远不落在模板文件里：

   ```text
   @FILE .dao/default.dao.json
   %EXPLAIN:
   {
     "url" : "=jdbc0.url",
     "password" : "=jdbc0.password",
     "maxActive": 50
   }
   %END%
   ```

6. **regapi 接口声明**：`@API` + `'cross/json'` 快捷元数据，
   内容就是接口的命令模板（通常一行 `jsc`）：

   ```text
   @API .regapi/api/attachment/create 'cross/json'
   %COPY:
   jsc ~/jsbin/api/create_attachment.js -vars \
     '{html:"id:${id}", tags:"${http-qs-tags?}"}'
   %END%
   ```

7. **目录与映射**：业务数据目录、序列号父目录、IO 映射目录
   （`{mnt:"..."}` 元数据，见 ../20.io-storage/10-io-mapping.md）。

8. **收尾：`@HOME` + `@ENV`**。给主目录打标题图标，把用户环境变量
   （默认应用、主题等）设置好，放在模板最后：

   ```text
   @HOME
   {title : "${domainName}", icon : "${domainIcon?fas-globe-asia}"}
   @ENV
   {OPEN : "wn-term", theme : "light"}
   ```

## 编写约定与坑

- **敏感信息只出现在 vars.json**：模板里永远写 `${变量}` 或 `=key` 引用，
  不出现明文密码——这是整个机制存在的主要理由
- **内联内容必须以 `%END%` 收尾**：漏写了会把后面的条目吞进内容里
- **`?` 与 `%` 的选择**：跟着项目仓库走的内容用 `%`（每次 app init
  都刷新到最新）；用户可能自己改过的用 `?`（只填空，不覆盖）。
  默认（行首什么都不写）也是只填空
- **`@DIR` 也可以带内容与元数据**：比如 demo.init 里
  `@DIR www/login/ '...' -> /mnt/...` 后面跟一大段多行 JSON——
  目录对象的元数据一样可以被消费（这里被登录站点配置读取）
- **`->` 链接优先于复制**：项目资源变了，链接自动生效；
  复制进域的副本要靠重跑 `app init` 且内容行是 `%` 才刷新
- **元数据行是 JSON**：多行写法和单行 `{}` 等价，长配置用多行更可读；
  值支持 `${var}` 与 explain 引用（`=key`），语法见
  10-init-domain-app.md 的「值的高级写法」
- **写完自测一遍**：在测试域里改 vars.json → 跑 `app init` →
  抽查几个关键对象（`o .dao/default.dao.json @json -cqn`），
  确认渲染结果符合预期

# 延伸阅读

- 10-init-domain-app.md —— 机制与标记语法的完整说明：命令流程、
  模板查找顺序、条目语法、explain 引用
- ../50.regapi/README.md —— `@API` 条目声明的接口机制
- ../20.io-storage/10-io-mapping.md —— `{mnt:...}` 元数据的映射语法
- cmd_app_init man 文档：
  ../../../src/com/site0/walnut/ext/data/app/man/zh_cn/cmd_app_init.md
