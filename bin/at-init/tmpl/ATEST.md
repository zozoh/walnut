# ATEST — 本项目的运行时集成测试手册

> 由 `walnut/bin/at-init/at-init.py` 生成（模板：`bin/at-init/tmpl/ATEST.md`）。
> `<!-- at-init:begin -->` 与 `<!-- at-init:end -->` 之间是**工具维护段**：`--force`
> 会按模板刷新它，段外内容永远不动；人工补充的说明写在段外。
>
> 与 JUnit 的分工：JUnit 管实现，ATEST 管对外行为（命令 / HTTP / jsc）。
> 凭据不写进本文档，放 `.at/credentials.json`（已 gitignore）。

<!-- at-init:begin（工具维护段：--force 会刷新；段外内容不受影响） -->
## 集成测试（ATEST）

- project: {{project}}
- instance: L2            # L1=IDE 里的实例（快速回路）/ L2=隔离实例（完整回归与基线）；端口见 .wn-instances.json
- test_domain: atest      # 本项目专用测试域：只增不减，跨用例复用
- credentials_file: .at/credentials.json
- init_template: test/atest.init
- sql_datasource: default # 域内 .dao 配置名，数据操作走 sqlx

套件启动前清缓存（`jsc -clear`、`sqlx @cache -clear`）；失败必须分诊为
bug / drift / env / flaky 之一并附证据；基线只在 2 级实例上建立与更新。
<!-- at-init:end -->

## 项目备注

（段外内容：写本项目的特殊约定、reset 步骤、静态布局说明等；`--force` 不会动这里。）
