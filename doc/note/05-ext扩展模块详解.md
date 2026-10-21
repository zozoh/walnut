# ext 扩展模块详解（1,167 个文件，85 个命令）

> ⚠️ 弃用中（作者 2026-10 确认）：**www 站点模型、`thing` 数据集概念**。cmd_www 已删除；cmd_thing 尚在但不再演进。
> 下表中 thing/titanium(站点UI)/www 相关行仅作现状记录，文档不投入。

## ext/data（421 文件）——数据与业务核心

| 子包 | 文件数 | 作用 |
|------|--------|------|
| thing | 53 | 通用数据集 ThingSet：索引/数据/媒体/附件/评论目录结构，thing.js 配置（uniqueKeys/fields/linkKeys/onXXX 钩子/导入导出/搜索界面配置） |
| sqlx | 52 | SQL 操作命令：在域内直接执行 SQL（配合 .dao 数据源） |
| fake | 50 | 假数据生成器（测试/演示用） |
| o | 47 | 对象增强操作（obj 命令扩展） |
| titanium | 45 | ti 管理界面的服务端支持（.ti 目录、sidebar、app 加载） |
| wf | 25 | 工作流（审批流？） |
| entity | 19 | 通用实体：like/favor/score（Redis）+ history/comment/newsfeed（SQL） |
| app | 18 | 域应用初始化（app init，@DIR/@FILE/@THING/@API/@ENV/@HOME/@INCLUDE 段语法） |
| esi | 12 | ESI 边缘包含？（待细读） |
| archive | 13 | 归档（zip/tar 打包备份，bulk 体系） |
| bizhook | 7 | 业务钩子（区别于系统钩子 hookx） |
| pvg | 7 | 业务权限模型服务（WnPvgMoreService + pvg 命令） |
| captcha/vcode/val/tmpl/expl/domain | ~30 | 验证码、值生成、模板、域名映射 |

## ext/sys（231 文件）——系统机制

| 子包 | 文件数 | 作用 |
|------|--------|------|
| redis | 54 | Redis 客户端封装 + 多数据源 |
| schedule+task+cron+crontab | 44 | 定期任务三件套：cron 表达式(分钟槽展开) / schedule 分钟计划 / task 任务栈 + crontab 兼容 |
| dsync | 18 | 数据同步（树索引 .tree + 包 pkg + 增量） |
| truck | 13 | 搬运：域间数据搬迁 |
| httpapi | 10 | HTTP 注册接口（API 对象→命令模板，请求对象注入 http-qs-*/http-header-* 元数据，缓存/权限/历史/preload） |
| hookx | 8 | 系统钩子执行（hook 命令） |
| lock | 8 | 锁命令 |
| mq+mqttc | 20 | 消息队列（CMD 消息 + 消费线程）+ MQTT 客户端 |
| mgadmin | 8 | MongoDB 管理命令 |
| refer | 8 | 桶引用计数 |
| websocket | 10 | WebSocket 服务（ws 命令） |
| wup | 11 | 更新包（Walnut Update Package，客户端升级） |
| trash | 4 | 回收站 |
| datex | 12 | 日期表达式 |
| aclog/i18n/os/path/sql/dirm | ~20 | 访问日志、多语言、系统信息、路径解析 |

## ext/media（196 文件）——媒体处理

| 子包 | 作用 |
|------|------|
| edi | 39 | EDI 电子数据交换？（最大媒体子包，待细读——可能是在线文档编辑器相关） |
| ooml | 25 | Office 模板（docx 占位符渲染 ${}、#loop、图片 alt 占位） |
| dom | 15 | HTML DOM 操作（jsoup） |
| sheet | 17 | 表格导入导出（csv/xls） |
| mediax | 22 | 媒体平台抽象 API（post/crawl/fetch/download） |
| imagic | 8 | 图像处理（裁剪/缩放/水印） |
| subtitle | 8 | 字幕（srt/ass）处理 |
| ffmpeg | 3 | 视频转码（依赖外部 ffmpeg） |
| qrcode/pdf/avatar/tpassport | 10 | 二维码、PDF、头像、护照/证件？ |

## ext/net（203 文件）——网络与第三方

| 子包 | 作用 |
|------|------|
| mailx | 45 | 邮件（多账户、模板、发送） |
| http+webx | 37 | HTTP 客户端 + 网页抓取/分析 |
| xapi | 25 | 通用第三方 API 整合（ThirdXApi：配置驱动接入任意 HTTP API，动态票据） |
| payment | 22 | 通用支付模型（WGPM：支付单 /var/payment，buy/pay 命令，多平台回调 re_wx/re_zfb/re_paypal） |
| alipay | 8 | 支付宝 |
| aliyun | 12 | 阿里云 OSS/VOD |
| sms | 10 | 短信（多供应商） |
| ftp/mqttc/socket | 19 | FTP、MQTT、Socket |
| icp/whoisx/ip2region/ipv4/ping | 12 | 备案查询、WHOIS、IP 归属、IPv4 工具 |

## ext/util（72）与 ext/geo（11）与 ext/xo（33）

- util：base64/btea(加密)/crc/hexbin/jsonx/react(响应式？)/seq/shorturl/strx —— 纯工具命令。
- geo：lbs（国家/中国行政区编码、运费计算 LbsFreight）。
- xo：XO 数据集框架（XoFilter/XoContext：对象级通用 CRUD 服务化，供 HTTPAPI 快速挂数据接口）。

## 文档与代码的对应缺口（重点）

- **thing（53 文件）**：⚠️ 概念已弃用，不再投入文档。存量资料：17 篇 man 手册（ext/data/thing/man/zh_cn/）可用作命令参考；th0-* 三篇正式文档标记归档。
- **titanium（45 文件）**：与站点 UI 体系绑定，随站点模型一并降级为维护模式，暂不投入系统文档。
- **sqlx/wf/fake/truck/edi**：完全无文档 → 成为数据操作方向的重点缺口。
- **httpapi（10 文件）**：正式文档 c1-regapi.md 极详尽（935 行），是文档与代码同步的正面案例。
- **支付**：f0-payment + f1-payment 双文档内容重复/冲突（f0 在 func-l0，f1 在 func-l1，key 却都是 f1-pay），draft/ext/payment 5 篇需归并。
