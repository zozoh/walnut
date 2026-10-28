# AppModule 重构说明

日期：2026-10-28
分支：OTP-Captcha

## 背景

`AppModule` 原本 834 行，12 个 `@At` 入口函数混在一起。本次重构把逻辑复杂的入口函数抽出为独立服务类，入口函数只保留参数签名和注解。

## 铁律（重构约束）

- 所有 `@At` 路径、`@Filters` / `@Ok` / `@Fail` 注解、参数列表**一律不动**
- 对外行为完全不变：响应格式、Cookie 写入、重定向、状态码、错误码保持逐字节等价
- 服务类模式参照 `WnLoginPage`：普通 Bean + setter 注入 + 一个主方法

## 服务类对照表

| 服务类 | 入口函数 | 原行数 |
|---|---|---|
| `web/module/app/WnAppModuleOpen` | `open()` | ~80 |
| `web/module/app/WnAppModuleLoad` | `load()` | ~65 |
| `web/module/app/WnAppModuleRun` | `run()` | ~55 |
| `web/module/app/WnAppModuleSysLoginByPassword` | `sys_login_by_passwd()` | ~50 |
| `web/module/app/WnAppModuleAuthLoginByPassword` | `auth_login_by_domain_passwd()` | ~150 |

## 其他修改

- `AbstractWnModule.try_run_profile`：`protected` → `public`（两个登录服务类需要调用）
- `AppModule.open()` 中 24 行注释死代码删除
- `AppModule.load()` 中 `_check_app_home(appName)` 保留在入口（依赖会话上下文），服务类接收查好的 `appHome`；副作用仅是 debug 日志少一个 `sw.tag("appHome ...")` 计时点

## 已知遗留（未动）

- `load()` 中 `text` 恒为 null 的 JSON 格式化分支：疑似死代码，但因"行为不能变"约束未删，待确认后单独处理
- 6 处入口重复出现的 `setCrossDomainHeaders` + `isRequestOptions` 样板代码，待抽公共 helper
