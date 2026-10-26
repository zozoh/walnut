---
title: 支付集成
module: 70.integrations
keywords: [pay, 支付单, WnPayment, wx.qrcode, zfb.qrcode, 回调, pay re]
source: [ext/net/payment/]
related: [weixin, alipay-aliyun, ../50.regapi/api-object.md]
---

# 支付集成

Walnut 内置统一的支付单模型（`WnPayObj` / `WnPay3x`），一个 `pay` 命令族对接多个支付渠道。源码在 `src/com/site0/walnut/ext/net/payment/`。

## 支付单模型

支付单就是 `/var/payment/` 下的普通对象，关键元数据（常量见 `WnPays`）：

- **`brief`**：支付简介
- **`pay_st`**：状态：`NEW`（已创建）/ `WAIT`（等待支付）/ `OK`（成功）/ `FAIL`（失败）
- **`pay_tp`**：支付类型：`wx.qrcode / wx.jsapi / wx.scan / zfb.qrcode / zfb.scan / ...`
- **`pay_target`**：目标商户（发送时指定）
- **`fee` / `cur`**：金额（分）/ 货币（默认 RMB）
- **`buyer_id` / `buyer_nm` / `buyer_tp`**：买家（`walnut:用户ID` 或 `域账户库ID`）
- **`seller_id` / `seller_nm`**：卖家（默认操作者所在域）
- **`send_at` / `close_at`**：发送时间 / 关闭时间
- **`apply_at` / `apply_re`**：回调执行时间 / 回调输出（防重复回调）
- **`re_tp` / `re_obj`**：支付返回的数据类型（`LINK/QRCODE/JSON/TEXT`）与数据
- **`pay_return_url`**：支付完成后的跳转 URL

**支付单文件内容**是回调命令模板——支付成功后以执行者身份执行（见下文回调）。

## 六个子命令

```bash
pay create    # 创建支付单
pay send      # 向第三方平台发起支付（拿到二维码/链接）
pay check     # 主动向平台查询支付状态
pay query     # 按条件查询一组支付单
pay get       # 支付单详情
pay re        # 统一处理平台回调
```

### 创建与发送

```bash
# 创建（金额单位：分）
pay create -br "年度会员" -bu walnut:45g..67q -fee 9900

# 创建并立即发起（-pt 支付类型，-ta 目标商户）
pay create -br "年度会员" -bu walnut:45g..67q -sl site0 -fee 9900 \
           -pt zfb.qrcode -ta mystore

# 带回调脚本：支付单内容会被当作回调命令执行
echo 'o /data/orders -match "{pid:\"${id}\"}" -u "{st:1}"' \
  | pay create -br "订单W20261021001" -bu walnut:xxx -fee 12800 -callback xxx
```

### 回调（pay re）

所有渠道的平台回调都收口到 `pay re`：

```bash
pay re [JSON] -idkey <ID键> [-s]     # -s 输出 success 给平台确认
httpparam -in id:xxx | pay re 45..e3 -s
```

- 幂等：支付单已成功且 `apply_at>0` 时，不重复执行回调命令，但输出相同结果；
- 回调成功后通过 `websocket event id:<支付单id> done` 向前端推送事件。

### 状态核对

```bash
pay check <id>     # 主动问平台"到底付了没"（对账用）
pay get <id>
pay query -match '{pay_st:"WAIT", lm:{$lt:两小时前}}'   # 找出超时未付的
```

## 渠道实现状态

- **支付宝二维码** — 实现：`alipay/ZfbQrcodePay3x` — 状态：✅ 启用
- **支付宝条码** — 实现：`alipay/ZfbScanPay3x` — 状态：✅ 启用
- **内部免费支付** — 实现：`free/FreePay3x` — 状态：✅ 启用（测试/内部结算）
- **PayPal** — 实现：`paypal/PaypalPay3x` — 状态：✅ 代码在（需自验配置）
- **微信三种** — 实现：`weixin/Wx*.java.txt` — 状态：⚠️ **源码被停用**（文件改为 .txt 后缀），启用微信渠道需恢复这些文件并补齐商户配置

## 典型场景：扫码收款闭环

```bash
# 1. API 创建支付单并返回二维码内容（re_tp=QRCODE）
#    regapi: http-output:json; 命令: pay create ... -pt zfb.qrcode -ta x
# 2. 平台回调 → regapi → pay re -idkey out_trade_no -s
# 3. 回调命令改订单状态 + websocket 通知前端刷新
```

## 注意事项

- `fee` 是**分**（9900 = 99 元）；
- 回调入口 API 必须公网可达，且校验平台签名（`alipay` 命令可辅助）；
- 定时 `pay check` + `pay query` 做对账，处理「平台已扣款但回调丢失」的掉单。

## 延伸阅读

- xapi — 更多第三方 API 走 xapi 框架
- API 对象 — 回调接口的实现方式
