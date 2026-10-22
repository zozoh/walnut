---
title: 命令执行模型
module: impl.box
keywords: [命令, 内置命令, 扩展命令, 简单命令, 过滤命令, 复杂命令, man]
source:
  - impl/box/JvmExecutorFactory.java
  - impl/box/JvmFilterExecutor.java
  - impl/box/JvmHdlExecutor.java
  - impl/box/WnSystem.java
related: [command-pipeline, custom-command, o, sqlx]
---

# 命令执行模型

Walnut 共有 **161 个命令**：**内置命令** 76 个（`impl/box/cmd/cmd_*.java`）+ **扩展命令** 85 个（`ext/**/cmd_*.java`）。两者调用方式完全一致，只是代码位置与维护归属不同。

## 三种命令写法

按实现继承关系，命令分三型——写命令或读源码时先判断它属于哪一型：

- ****简单命令**** — 基类：`JvmExecutor` — 形态：直接实现 `exec(sys, args)`，参数用 `ZParams` 解析 — 例子：`cmd_echo`、`cmd_cat`
- ****过滤命令**** — 基类：`JvmFilterExecutor` — 形态：维护一个上下文对象，`@子命令` 按管道依次加工上下文 — 例子：`cmd_o`、`cmd_jsonx`、`cmd_tmpl`
- ****复杂命令**** — 基类：`JvmHdlExecutor` — 形态：子命令是 `hdl/` 目录下的 `JvmHdl` 类；关键逻辑封装成服务类，供其他模块复用 — 例子：`cmd_sqlx`、`cmd_cron`、`cmd_xapi`

```bash
echo hello > ~/a.txt        # 简单命令：参数直接消费
o ~/docs @query -l @json    # 过滤命令：@query 填上下文，@json 输出
sqlx @vars '{id:1}' @query pet.query   # 复杂命令：子命令处理器 + SqlProcessor 服务
```

简单命令适合单一功能；过滤命令适合「输入→多步加工→输出」；复杂命令在过滤命令基础上把核心逻辑抽成服务（如 sqlx 的 `SqlProcessor` 被 SqlIndexer、SqlBM、登录会话存储复用），命令本身只是入口壳。

## 内置命令（76 个）

`impl/box/cmd/`，随系统发布、不可卸载：

- **`adduser`**：添加用户
- **`ajaxre`**：把输出包装成 ajaxReturn 格式
- **`alias`**：命令别名
- **`cat`**：读取/输出文件内容，支持拼接
- **`cd`**：切换当前目录
- **`chgrp`**：修改对象所属组
- **`chimg`**：修改图片对象（缩略图等）
- **`chmod`**：修改对象权限位（+r/w/x）
- **`chown`**：修改对象属主
- **`chxxx`**：通用元数据修改
- **`cmds`**：列出当前环境全部可用命令
- **`code`**：查看代码文件内容
- **`count`**：统计路径下文件数量
- **`cp`**：复制对象
- **`cpobj`**：复制对象（保留元数据完整版）
- **`curl`**：HTTP 下载/请求（类 Linux curl）
- **`date`**：显示/格式化日期时间
- **`defthumbnail`**：设置目录默认缩略图
- **`dircount`**：子项计数
- **`disk`**：存储配额与用量
- **`du`**：统计目录大小
- **`echo`**：输出文本（支持重定向写文件）
- **`env`**：查看环境变量
- **`exit`**：退出当前 shell/会话
- **`export`**：设置会话环境变量
- **`find`**：按名称/类型/时间/元数据查找对象
- **`fnm`**：文件名模式工具
- **`grep`**：文本过滤匹配
- **`grp`**：用户组管理
- **`hex`**：十六进制 dump
- **`hook`**：列出系统钩子
- **`httpout`**：HTTP 响应输出控制（API 场景）
- **`httpparam`**：HTTP 请求参数解析/回显（API 调试）
- **`iimg`**：读取图片信息
- **`image`**：图片相关操作
- **`ioc`**：查看 IoC 容器中的对象
- **`jsc`**：执行 JS 脚本文件
- **`json`**：JSON 格式化（stdin → 美化/紧凑）
- **`jvmMemory`**：查看 JVM 内存状态
- **`ll`**：详细列表（含元数据）
- **`ln`**：创建链接对象
- **`login`**：登录并创建会话
- **`ls`**：列出目录内容
- **`man`**：查看命令手册
- **`md5sum`**：MD5 摘要
- **`me`**：当前用户信息与变量
- **`mkdir`**：创建目录
- **`mode`**：权限模式操作
- **`mount`**：设置目录映射（mnt 元数据）
- **`mv`**：移动/重命名
- **`obj`**：⚠️ 旧对象命令，已弃用，用 `o` 替代
- **`output`**：延迟/格式化输出
- **`passwd`**：修改密码
- **`path`**：路径工具
- **`pwd`**：显示当前目录
- **`random`**：生成随机串
- **`rename`**：重命名对象
- **`resetrootpasswd`**：重置 root 密码
- **`rm`**：删除对象
- **`run`**：把 stdin 逐行当命令执行（批量执行引擎）
- **`session`**：会话管理
- **`sha1sum`**：SHA1 摘要
- **`str`**：字符串转换
- **`sys`**：系统信息
- **`textTable`**：文本表格输出
- **`time`**：计时执行命令（性能测量）
- **`touch`**：创建空文件/刷新时间
- **`trim`**：修剪文本首尾
- **`umount`**：移除目录映射
- **`unzip`**：解压缩包
- **`uuid`**：生成 UUID
- **`wget`**：HTTP 下载
- **`whoami`**：当前用户身份
- **`wordt`**：文字模板替换
- **`xxxsum`**：多算法摘要（md5/sha1 等）
- **`zip`**：打包压缩

## 扩展命令（85 个）

`ext/**/cmd_*.java`，按领域分包：

### data（22）

- **`o`**：对象操作瑞士军刀：CRUD/聚合/树形/批量（见 [o](o.md)）
- **`sqlx`**：SQL 数据库网关：模板 SQL + 变量/事务/乐观锁（见 [sqlx](sqlx.md)）
- **`tmpl`**：文本模板渲染：占位/循环/分支（见 [tmpl](tmpl.md)）
- **`val`**：值生成器（ID/序列/随机值）
- **`vcode`**：验证码：生成、发送（短信/邮箱）、校验
- **`fake`**：生成模拟测试数据
- **`expl`**：解释执行表达式（=取值、->转换等 WnExplain 语法）
- **`pvg`**：业务权限（角色×动作）配置与检查
- **`app`**：为界面（obrowser）提供应用信息
- **`archive`**：归档文件处理（zip/tar/tgz）
- **`bizhook`**：按配置文件对对象执行业务钩子命令
- **`captcha`**：生成图形验证码
- **`domain`**：系统域名（/domain）管理
- **`buy`**：购物车管理
- **`favor`**：收藏管理
- **`like`**：点赞管理
- **`score`**：打分管理
- **`esi`**：Elasticsearch 交互
- **`ti`**：Titanium 框架（界面层）服务器操作
- **`unzipx`**：压缩包解包/预览
- **`wf`**：工作流（过滤型，加载流程配置执行）
- **`thing`**：⚠️ Thing 数据集，已弃用仅维护，用 `o` 替代

### geo（1）

| 命令 | 说明 |
|------|------|
| `lbs` | 位置服务（LBS）操作 |

### media（13）

- **`imagic`**：图像处理（缩放/裁剪/镜像等过滤器）
- **`videoc`**：视频转码（ffmpeg），产出预览视频/预览图
- **`videoi`**：读取视频元信息
- **`qrcode`**：二维码生成与解析
- **`sheet`**：JSON 数组 ⇄ 表格（csv/xls）转换，字段映射
- **`ooml`**：Office 模板渲染（docx/xlsx 占位与循环）
- **`dom`**：jsoup 风格 HTML DOM 过滤操作
- **`edi`**：EDI 报文解析/渲染
- **`subtitle`**：字幕转换（srt）
- **`avatar`**：文字头像生成
- **`mediax`**：媒体抓取/下载/投递
- **`pdf`**：PDF 处理（hdl 子命令）
- **`tpassport`**：证件照排版绘制

### net（17）

- **`httpc`**：HTTP 客户端（@过滤器组装请求）
- **`xapi`**：第三方平台 API 调用框架（专家配置）
- **`mailx`**：邮件收发：SMTP/IMAP/模板/附件/S-MIME（见 [mailx](mailx.md)）
- **`sms`**：短信发送与查询
- **`webx`**：站点用户自助：登录/会话/改密/绑定（见 [webx](webx.md)）
- **`pay`**：通用支付单（微信/支付宝/PayPal/内部）
- **`alipay`**：老支付宝接口（支付表单/回调验签）
- **`aliyunoss`**：阿里云 OSS 文件操作
- **`aliyunvod`**：阿里云 VOD（播放凭证/搜索）
- **`ftp`**：FTP 客户端
- **`mqttc`**：MQTT 客户端（pub/sub）
- **`whoisx`**：域名 WHOIS 友好查询
- **`icp`**：ICP 备案查询
- **`ip2region`**：IP 归属地查询
- **`ipv4`**：IPv4 地址工具
- **`ping`**：网络连通探测
- **`socket`**：socket 通信

### sys（22）

- **`cron`**：定期任务（cron_task）管理
- **`schedule`**：分钟计划表（schedule_task）管理
- **`task`**：后台任务栈（task）管理
- **`mq`**：系统消息队列（发送/密钥）
- **`lock`**：全局锁（申请/释放/查看）
- **`hookx`**：钩子调试与触发
- **`trash`**：回收站（mv/recover）
- **`refer`**：内容引用计数
- **`mgadmin`**：MongoDB 直连管理（root）
- **`redis`**：Redis 客户端（KV/哈希/集合操作）
- **`aclog`**：访问日志读写/分析
- **`datex`**：日期时间过滤器管道
- **`dsync`**：目录同步与归档（树索引/打包/恢复）
- **`truck`**：索引/桶管理器间数据迁移
- **`httpapi`**：regapi 数据操作
- **`crontab`**：WnCrontabService 控制
- **`dirm`**：目录映射管理
- **`i18n`**：本地化字符串支持
- **`os`**：系统信息（字体等）
- **`path`**：路径工具
- **`websocket`**：WebSocket 操作（事件推送）
- **`wup`**：通用更新服务

### util（9）

| 命令 | 说明 |
|------|------|
| `jsonx` | JSON 过滤器管道（30+ @过滤器） |
| `strx` | 字符串过滤器管道 |
| `base64` | base64 编码/解码（管道） |
| `hexbin` | 二进制 ⇄ HEX（管道） |
| `btea` | btea 对称加解密（管道） |
| `crc` | CRC 校验 |
| `seq` | 序列生成器（批量编号） |
| `react` | 数据联动修改（配置 + 变量触发） |
| `shorturl` | 短链接生成 |

### xo（1）

- **`xo`**：对象数据集框架：把目录封装成标准 REST 数据集（CRUD + 挂 API）

## 帮助系统（man）

每个命令配中文手册，运行时查：

```bash
man o            # o 总览（含全部 @子命令列表）
man o -k size    # 在手册里搜关键词
man sqlx query   # 子命令手册
```

手册源文件（可直接阅读）：
- 内置：`src/com/site0/walnut/impl/box/cmd/man/zh_cn/`
- 扩展：各 `src/com/site0/walnut/ext/**/man/zh_cn/`（含每个子命令页）

## 输出控制惯例

- **`-c`**：紧凑输出
- **`-q`**：键名不加引号
- **`-n`**：忽略 null 字段
- **`-json`**：输出 JSON 文本
- **`-ajax`**：Ajax 返回包裹 `{ok,data}`
- **`-t f1,f2`**：指定输出字段
- **`-l`**：强制列表输出
- **`-quiet`**：不输出（配合批量）

细节以各命令 man 为准。

## 命令查找顺序

```bash
输入 o ...
  1. ~/bin/o(.js|.sh|...) 存在？→ 用用户的（覆盖 JVM 命令）
  2. 否则加载 JVM 类 cmd_o
```

`PATH` 环境变量决定查找路径（默认 `/bin:/sbin:~/bin`）；`alias` 可建别名。

## 排错

```bash
cmds | grep xx        # 有哪些相近命令（没有 which 命令）
man xx                # 先看手册再猜参数
o xxx -debug          # 对象命令看完整元数据
```

## 延伸阅读

- [命令管道概念](../10.concepts/command-pipeline.md)
- [自定义命令](../90.dev/custom-command.md)
- 重点命令专篇：[o](o.md) / [sqlx](sqlx.md) / [webx](webx.md) / [mailx](mailx.md) / [tmpl](tmpl.md)
