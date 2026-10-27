---
title: SMS 短信（占位）
module: 70.functionate
keywords: [sms, 短信, 占位]
source: [ext/net/sms/]
related: [README]
---

# SMS 短信（占位）

> ⚠️ 本篇为占位文档。SMS 模块计划重写，重写完成后补充正式内容。

现状速览（重写前的机制，仅供参考）：

- 命令 `sms`（发送 / `@query` 查询发送状态），provider 插件机制
- 配置文件 `$HOME/.sms/config_<provider>`（JSON，公共字段 `lang`、`timeout`）
- 现有 provider：`Yunpian`（云片网，默认）、`Submail`（赛邮）
