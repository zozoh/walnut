---
caseID: CASE-302
title: 参数变化改变落库对象，同一条连跑两次仍幂等
source: ATESET-10 / wn-doc 101.dev/20-write-jsbin §脚本的输出契约
contracts:
  - POST /api/demo/order/create?amount=5
  - ~/jsbin/order_flow.js
  - cmd_o @create
expected: inline
---

# CASE-302 参数变化改变落库对象，同一条连跑两次仍幂等

## 前置

- 与 CASE-301 同一条链路：HTTP → `jsc`（jsbin）→ `o @create`；
  夹具是同一份，只是请求参数换成 `amount=5`。
- `amount` 从查询串经接口模板的 `${http-qs-amount?}` 传进 jsbin，
  再拼进对象名：所以参数变化**应当**改变落库对象（`order-3.json` → `order-5.json`）。
- 命令带 `-upsert`，同一条请求连跑两次不会撞"已存在"；第二次是覆盖，
  不是新建，所以同名对象始终只有一个。
- "唯一"不靠返回值判断：用 `o @count` 在父目录里数同名对象，
  再 `snap` 读一次对象确认内容。

## 步骤

1. 铺夹具：`atlib.fixture()` —— 与 CASE-301 共用 jsbin 脚本与接口对象；
2. 第一次发请求：`atlib.http("/api/demo/order/create?amount=5", method="POST")`，
   取回状态码与 `data`；
3. 第二次发同样的请求，再取一次状态码与 `data`；
4. 读副作用对象：`atlib.snap("~/atest-lab/orders/order-5.json")`；
5. 数同名对象：`o ~/atest-lab/orders @count '{nm:"order-5.json"}'`；
6. 把两次的状态码/信封值、对象值、同名数量一起 `emit()`。

## 期望

```json
{
  "first_status": 200,
  "second_status": 200,
  "first_nm": "order-5.json",
  "second_nm": "order-5.json",
  "first_amount": 5,
  "second_amount": 5,
  "obj_nm": "order-5.json",
  "obj_amount": 5,
  "same_nm_count": 1
}
```

## 证据

1. `evidence/summary.txt`：判定结果与脚本逐步记录（含同名计数）；
2. `evidence/http/*.txt`：两次请求各自的原始响应；
3. `evidence/objects/~__atest-lab__orders__order-5.json.json`：第二次之后的副作用对象快照；
4. `evidence/result.json` / `stdout.txt` / `stderr.txt`：脚本结果与原始输出。
