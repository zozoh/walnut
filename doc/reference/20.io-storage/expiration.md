---
title: 过期对象机制
module: core.eot
keywords: [expi, 过期, EOT, 清理线程, 多节点, expi-obj-clean]
source:
  - api/io/WnExpiObjTable.java
  - core/eot/
  - web/clean/WnExpiObjTableCleaner.java
  - web/clean/WnIoCleaner.java
related: [io-model, everything-is-obj, metadata-conventions]
---

# 过期对象机制

给对象设置 `expi`（过期绝对毫秒）后就不用管了：系统到期**自动删除**。会话、验证码、订单、临时请求对象都靠这个机制自愈，不需要业务自己起清理线程。

## 工作原理

```bash
创建/修改对象，元数据带 expi
   │
   ▼
EOT 表（Expired Object Table）登记 {id, expi, hold, ow}
   │
   ▼
清理线程（每节点一个，间隔 expi-obj-clean-interval，默认 60s）
   ├─ 抢锁（WnLockApi，多节点只有一个干活）
   ├─ 批量取已到期的记录（上限 expi-obj-clean-limit，默认 100）
   ├─ 占用：hold=现在+interval, ow=本节点（防其他节点重复处理）
   ├─ 删除对象本体（走正常删除，触发钩子）
   └─ 清掉 EOT 记录
```

`hold` 字段是**多节点竞争的关键**：一个节点领取记录后设置占用窗口，窗口内其他节点跳过；节点崩溃则窗口过期后由别的节点接手。

## 配置（web.properties）

```properties
# 清理线程启动间隔（毫秒）
expi-obj-clean-interval=60000
# 每轮处理对象数上限
expi-obj-clean-limit=100
# 记录占用时间（毫秒），一般与 interval 相同
expi-obj-clean-hold=60000
```

另有 `WnCleanExpiObjTableSetup` 定期清理 EOT 表自身的僵尸记录。

## 用法

```bash
# 设置 1 小时后过期
o ~/tmp/session.json -u 'expi:%ms:now+1h'
# %ms:now+1h 是时间表达式，见 datex

# 永不 Exp（默认 0）
o ~/tmp/session.json -u 'expi:0'

# 查看快过期的对象
o ~ -match 'expi:[1,%ms:now+300000]' -cqn
```

JS 里：

```js
var o = sys.io.create(null, "~/tmp/x.json", Wn.Race.FILE); // Race 常量见 jsexec-api
sys.io.appendMeta(o, {expi: Date.now() + 3600_000});
```

## 典型场景

| 场景 | 做法 |
|------|------|
| 会话 | 登录时按 `se-sys-du` 设 expi，touch 时顺延 |
| 验证码 | `~/../vcode/{域}/{场景}/{账号}`，10 分钟过期 |
| 订单超时关闭 | 创建订单设 expi，支付成功清除 expi |
| 临时请求对象 | regapi 请求对象默认缓存 1 分钟 |
| 通知记录 | 发送完成后设 expi，统一回收 |

## 注意事项

- 过期即**物理删除**，不可恢复（需要请提前移入归档或加状态字段代替）。
- `expi` 是元数据：修改元数据即重新登记 EOT；手动 `rm` 的对象，其 EOT 记录会在下一轮被清。
- 批量导入大量带 expi 的对象时，EOT 写入会有额外开销——非临时对象不要随手设 expi。
