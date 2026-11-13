---
caseID: CASE-301
title: 三层联动：HTTP 打接口、接口跑 jsbin、jsbin 用命令落库
source: ATESET-10 / wn-doc 101.dev/20-write-jsbin §脚本的输出契约
contracts:
  - POST /api/demo/order/create
  - ~/jsbin/order_flow.js
  - cmd_o @create
expected: inline
---

# CASE-301 三层联动：HTTP 打接口、接口跑 jsbin、jsbin 用命令落库

## 前置

- 夹具声明两件东西（用例脚本不负责造接口/脚本）：
  - `@FILE jsbin/order_flow.js`：ES5 脚本，读 `amount` → 拼 `order-<amount>.json`
    → `$wn.exec` 一条 `o @create -p ~/atest-lab/orders -upsert` → `$log.ajax_ok`；
  - `@API .regapi/api/order/create 'cross/json'`：内容是一条命令模板
    `jsc ~/jsbin/order_flow.js -vars '{amount:"${http-qs-amount?}"}'`。
- 三层各管一段：HTTP 只是入口，`jsc` 是 jsbin 执行层，`o @create` 是命令层；
  这条用例验的是**接缝**，不是某一层的语法细节。
- 脚本用 `$wn.exec` 而不是 `sys.exec`：命令输出作为返回值（不进脚本 stdout），
  这样 HTTP body 只有一个 AjaxResult 信封；命令失败时 `$wn.exec` 直接抛异常。
- `-upsert` 让同一对象连跑多次不撞"已存在"；对象名里带 `amount`，
  两条用例各自落到不同文件，互不覆盖。
- `~/atest-lab/orders/` 由夹具的 `@DIR` 铺好：`o @create -p` 不会自建父目录。

## 步骤

1. 铺夹具：`atlib.fixture()` —— 声明 jsbin 脚本与接口对象；
2. 发请求：`atlib.http("/api/demo/order/create?amount=3", method="POST")`，
   取回状态码、响应头与响应体；
3. 从响应体解析出 `ok` 与 `data.orderNm` / `data.amount`；
4. 读副作用对象：`atlib.snap("~/atest-lab/orders/order-3.json")`，取 `nm` 与 `amount`；
5. 把信封里的值与对象里的值一起 `emit()` 出去（两组值各带前缀，便于对比）。

## 期望

```json
{
  "status": 200,
  "ctype": {"$regex": "json"},
  "ok": true,
  "orderNm": "order-3.json",
  "amount": 3,
  "obj_nm": "order-3.json",
  "obj_amount": 3
}
```

## 证据

1. `evidence/summary.txt`：判定结果与脚本逐步记录；
2. `evidence/http/*.txt`：接口原始响应（状态行 + 响应头 + AjaxResult 信封）；
3. `evidence/objects/~__atest-lab__orders__order-3.json.json`：副作用对象快照；
4. `evidence/result.json` / `stdout.txt` / `stderr.txt`：脚本结果与原始输出。
