---
title:支付界面制作建议
author:zozoh
tags:
- 系统
- 支付
---

# 主动扫码二维码付款

适用类型:

- `zfb.qrcode`
- `wx.qrcode`

![](payment_gui_qrcode.png)


# 商户码枪扫码流程

适用类型:

- `zfb.scan`
- `wx.scan`

![](payment_gui_scan.png)

# 微信公众号内支付(wx.jsapi)

直接请求 `/pay/ajax/do` 即可得到微信支付的配置项。在网页里设置即可


