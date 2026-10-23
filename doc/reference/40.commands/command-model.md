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

Walnut 共有 **159 个命令**：**内置命令** 74 个（`impl/box/cmd/cmd_*.java`）+ **扩展命令** 85 个（`ext/**/cmd_*.java`）。两者调用方式完全一致，只是代码位置与维护归属不同。

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

## 内置命令（74 个）

`impl/box/cmd/`，随系统发布、不可卸载：

- **`adduser`**：添加用户
- **`ajaxre`**：包装成 AjaxReturn 格式输出
- **`alias`**：命令别名
- **`cat`**：输出文件内容
- **`cd`**：切换当前目录
- **`chgrp`**：修改对象属组
- **`chimg`**：图片转换（缩放/旋转/iOS 方向修正）
- **`chmod`**：修改对象权限位
- **`chown`**：修改对象属主
- **`cmds`**：列出全部可用命令
- **`code`**：管道内容按指定字符集编码输出
- **`count`**：统计目录子对象数量
- **`cp`**：拷贝对象
- **`cpobj`**：拷贝对象（元数据复制策略完整版）
- **`curl`**：发起 HTTP 请求
- **`date`**：显示/格式化日期时间
- **`defthumbnail`**：生成系统默认缩略图
- **`dircount`**：按深度统计目录子对象数量
- **`disk`**：列出目录子对象
- **`du`**：统计目录空间占用
- **`echo`**：输出文本
- **`env`**：显示会话环境变量
- **`exit`**：登出当前会话
- **`export`**：设置会话环境变量
- **`find`**：遍历目录树，输出相对路径
- **`fnm`**：生成不冲突的文件名
- **`grep`**：筛选匹配行（正则/子串）
- **`grp`**：用户组管理（查组员/增删成员）
- **`hex`**：十六进制查看
- **`hook`**：查询/重跑对象钩子
- **`httpout`**：输出标准 HTTP 响应
- **`httpparam`**：解析 HTTP 请求参数
- **`iimg`**：图片信息/缩略图/压缩
- **`image`**：读取图片信息（尺寸/EXIF）
- **`ioc`**：列出 IoC 容器 Bean
- **`jsc`**：执行 JS 脚本
- **`json`**：JSON 加工（取值/过滤/映射/合并）
- **`jvmMemory`**：查看 JVM 内存
- **`ll`**：`ls -l` 简写
- **`ln`**：创建链接（硬/软）
- **`login`**：创建子会话（su）
- **`ls`**：列出目录内容
- **`man`**：查看命令手册
- **`md5sum`**：MD5 摘要
- **`me`**：当前用户信息与变量
- **`mkdir`**：创建目录
- **`mode`**：权限码分析/换算
- **`mount`**：设置目录 mnt 映射
- **`mv`**：移动/重命名
- **`obj`**：⚠️ 旧对象命令，已弃用，用 `o` 替代
- **`output`**：延迟/批量输出（管道测试用）
- **`passwd`**：修改密码
- **`path`**：输出对象绝对路径
- **`pwd`**：显示当前目录
- **`random`**：生成随机数
- **`rename`**：重命名对象
- **`resetrootpasswd`**：重置 root 密码
- **`rm`**：删除对象
- **`run`**：执行命令模板（占位符替换）
- **`session`**：查看/列出会话
- **`sha1sum`**：SHA1 摘要
- **`str`**：字符串转换（URI 解码/去空白/替换）
- **`sys`**：查看系统级信息
- **`textTable`**：JSON 渲染文本表格
- **`time`**：计量命令耗时
- **`touch`**：创建文件/刷新时间戳
- **`trim`**：去除文本首尾空白
- **`umount`**：卸载目录 mnt 映射
- **`unzip`**：解压 zip 包
- **`uuid`**：生成 UUID
- **`wget`**：下载文件
- **`whoami`**：当前用户名
- **`wordt`**：Word 文档占位符替换
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
