---
title: 各支付平台的所需数据
author: wendal
tags:
- 系统
- 支付
---

## 仅罗列必选字段


| 字段含义            |       支付宝           |      微信                    |   说明                 |
|-----------|---------------|----------------|------------|
| 卖家id     |     seller_id |   appid        | 支付宝叫卖家id,微信叫公众账号ID |
|商户id      |      partner  |   mch_id        |支付服务的商户识别码|
|随机字符串           |      无                     |nonce_str        |统一使用R.UU32()|
|字符串编码           | _input_charset| 无                                       |统一用UTF-8|
|签名                      |    sign       |sign             |表单参数签名的Base64字符串|
|签名类型               |  sign_type    | sign_type       |统一使用MD5|
|商品描述               |    subject    | body            |支付宝限制在25个字以内|
|商户订单号           |  out_trade_no | out_trade_no    |支付宝要求64位以下,微信要求32位以下 |
|金额                      |  total_fee    | total_fee       | 支付宝单位是元,微信是分 |
|终端ip      |               | spbill_create_ip| 这个由walnut提供?  |
|回调地址              | notify_url    | notify_url      |             |
|交易类型              | payment_type  | trade_type      |微信扫码NATIVE,微信公众号JSAPI,支付宝固定为1|
|用户识别码          |               | openid          | 微信公众号支付时必选|

## 参考文档

* [支付宝即时到账](https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7629140.0.0.TDrDw6&treeId=62&articleId=104743&docType=1)
* [微信支付#统一下单API](https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_1)