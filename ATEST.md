# ATEST — 本项目 Agent 测试手册

> 本文档是 Agent 在本项目执行 Walnut 后台测试的唯一权威说明。
> 通用约定见 Walnut 文档库 wn-doc 的 `{WN_DOC_HOME}/reference/102.auto-test/` 系列
> （`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）。
> 凭据不要写进本文档，放 `.at/credentials.json`（已 gitignore）。

- walnut_url: http://localhost:8080
- walnut_level: 1            # 1=开发者的实例 / 2=开发者启动的共享实例 / 3=自己的私有实例
- walnut_dist: <级别2/3必填：dist/wn4j 路径>
- test_domain: demo
- credentials_file: .at/credentials.json
- init_template: <建域后执行 `app init` 的 .init 模板位置>
- sql_datasource: default    # 域内 .dao 配置名，数据操作走 sqlx

## 协作

<同域的其他 Agent（看各自的 .at/whoami.toml 代号）及分工；没有则写"无">

## 模拟数据策略

<测试数据怎么造：通道选择（cmd/obj/mnt/sqlx）、fixture 位置、批量灌数脚本>

## reset

<重置数据的具体步骤：清哪些表（sqlx）、删哪些对象（o @delete）>

<!-- at-init:begin（工具维护段：--force 会刷新；段外内容不受影响） -->
## 集成测试（ATEST）

- project: walnut
- instance: L2            # L1=IDE 里的实例（快速回路）/ L2=隔离实例（完整回归与基线）；端口见 .wn-instances.json
- test_domain: atest      # 本项目专用测试域：只增不减，跨用例复用
- credentials_file: .at/credentials.json
- init_template: test/atest.init
- sql_datasource: default # 域内 .dao 配置名，数据操作走 sqlx

套件启动前清缓存（`jsc -clear`、`sqlx @cache -clear`）；失败必须分诊为
bug / drift / env / flaky 之一并附证据；基线只在 2 级实例上建立与更新。
<!-- at-init:end -->
