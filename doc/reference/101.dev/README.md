---
title: 二次开发
module: 101.dev
keywords: [二次开发, jsbin, sqlx, regapi, 自定义命令, Java 嵌入]
related: [../40.commands/README.md]
---

# 二次开发概述

`Walnut` 本身是一个基座，真正的业务系统要在它之上开发。
本目录收录「如何把业务做进 Walnut」的各种开发指南。

开发 Walnut 业务有三条路，按改动深度由浅到深：

1. **写脚本（jsbin）**——业务逻辑写成 JS 脚本，改完即生效，这是最常用的路
2. **写配置（regapi / SQL 模板 / app 初始化）**——不写代码，
   用对象和模板把 API、数据表、应用骨架声明出来
3. **写 Java**——自定义命令、嵌入模式，框架级的扩展

```text
101.dev/
|-- README.md                # 本篇
|-- 10-write-jsbin.md        # 编写 jsbin 业务脚本（JS 脚本规范与场景示例）
|-- 11-write-sqlx-tmpl.md    # 编写 SQL 模板（sqlx 模板语言规范）
|-- 20-write-app-init.md     # 编写应用初始化模板（app init 声明式初始化）
|-- 30-extend-regapi.md      # 定制 HTTP API（regapi 扩展实战）
|-- 40-custom-command.md     # 自定义命令开发
|-- 50-write-java.md         # 直接添加 Java 模块（库模式嵌入）
```

## 怎么选路

- 业务规则经常变、性能要求一般 → jsbin（10-write-jsbin.md）
- 要拼动态 SQL → SQL 模板（11-write-sqlx-tmpl.md）
- 要给新用户初始化应用骨架 → app init 模板（20-write-app-init.md）
- 要对外提供 HTTP 接口 → regapi（30-extend-regapi.md）
- 要新增一个系统级能力、被其他命令调用 → 自定义命令（40-custom-command.md）
- 要做成一个产品级 Java 应用 → 库模式嵌入（50-write-java.md）
