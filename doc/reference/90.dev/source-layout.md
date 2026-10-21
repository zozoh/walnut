---
title: 源码包结构导览
module: 90.dev
keywords: [源码结构, 包结构, src, com.site0.walnut, 代码地图]
source: [src/]
related: [../SOURCEMAP.md, custom-command, jsexec-api]
---

# 源码包结构导览

Java 源码在 `src/`，根包 `com.site0.walnut`，另内嵌了精简版 Nutz（`src/org/nutz`，dao/ioc/mvc/json/lang 等）。这是 AI 读代码的地图——各包一句话 + 规模。

## 顶层包

| 包 | 类数 | 一句话 |
|---|---|---|
| `api/` | 44 | 核心接口：io / auth / hook / lock / box |
| `core/` | 99 | 核心实现（io 引擎、索引、桶） |
| `impl/` | 115 | 命令沙箱、业务实现、JS 钩子 |
| `util/` | 197 | Wn 工具集、模板、校验 |
| `ext/` | 1167 | **全部扩展命令**（按领域分包） |
| `web/` | 47 | MVC 主模块、过滤器、登录 |
| `login/` | 42 | 登录域模型（用户/角色/域账户库） |
| `jetty/` | 4 | 内嵌 jetty 与域名路由过滤器 |
| `cron/` | 13 | WnCron 表达式引擎 |
| `jsexec/` | 2 | JS 执行引擎（nashorn 封装、域级全局 JS 缓存） |
| `nashorn/` | 1 | Nashorn 对象包装 |
| `ooml/` | 34 | Office 模板引擎（独立子项目） |
| `cheap/` | 71 | Cheap 引擎（HTML→文档渲染，CMS 向） |
| `alg/` | 44 | 通用算法（区间、表达式、树） |
| `val/` | 18 | 值对象（动态值、百分比等） |
| `cache/` | 5 | 缓存抽象 |
| `lookup/` | 7 | 查找服务（类似 JNDI） |
| `security/` | 10 | 加密、摘要、验证码 |
| `jdbc/` | 5 | ⚠️ 已弃用（关系库 DAO 体系） |

## ext/ 扩展命令包（按域）

```text
ext/
|-- data/        数据类：pvg 权限、sqlx、expl/tmpl、domain、fake、esi ...
|-- media/       媒体类：imagic、ffmpeg(videoc/videoi)、qrcode、sheet、ooml、dom、edi、pdf
|-- net/         网络类：http(curl/wget/httpc)、xapi、mailx、sms、ftp、alipay、
|                aliyun(oss/vod)、payment(wx/zfb/free/paypal)、webx、mqttc、whoisx ...
|-- sys/         系统类：cron/schedule/task、mq、lock、hookx、trash、refer、
|                mgadmin、aclog、dsync、truck、datex、redis、path、httpapi、websocket
|-- util/        工具类：base64、hexbin、btea、crc、jsonx、strx、seq、react、shorturl
```

每个命令包的标准结构：`cmd_xxx.java`（入口）+ `hdl/`（子命令处理器）+ `man/zh_cn/`（中文手册）+ `impl/`（服务实现）。

## 其他重要位置

| 位置 | 内容 |
|---|---|
| `WebContent/WEB-INF/web.xml` | Servlet 过滤器链 |
| `conf/` | 运行配置（web.properties、ioc、hostmap） |
| `ROOT/` | 系统默认目录内容（init-mnt 挂载源） |
| `doc/_old/markdown/` | 历史参考文档（部分与源码脱节） |

## 给 AI Agent 的读码建议

1. 从 [SOURCEMAP.md](../SOURCEMAP.md) 反查「功能 → 包」；
2. 命令行为先看 `cmd_xxx.java`，子命令看 `hdl/`；
3. 元数据语义以使用它的类为准（grep 键名字符串）；
4. 手册页 `src/**/man/zh_cn/*.md` 是命令行为的最可靠文字描述。

## 延伸阅读

- [SOURCEMAP.md](../SOURCEMAP.md) — 双向映射
- [自定义命令](custom-command.md)
- [Java 库模式](java-lib.md)
