---
title: 微信整合·通知
author: zozoh
key: f0-wxnti
---

# 配置文件结构

```bash
~/.weixin
|-- zozoh/       # 微信配置目录
    |-- wxconf   # 微信公众号配置文件
```

# 微信后台配置

假设在微信后台，配置如下消息模板

```bash
# 模板ID
hii5UTOT6P7YnJjGN-9FiSCf2qmSjaZsKrTsjF-7ej8 	

# 模板标题
测试消息

# 模板内容
这里是测试消息 {{ta.DATA}}，以及测试文字 {{tb.DATA}} 哈哈 
```

# 执行消息的发送

```bash
weixin zozoh tmpl -send '{ta:"三个字儿",tb:"Hello!"}' \
  -tid hii5UTOT6P7YnJjGN-9FiSCf2qmSjaZsKrTsjF-7ej8 \
  -to okyjlsv00EwLDRHnf_vWoq2iKJaE \
  -url http://nutz.cn
```

- `tid` 为微信消息模板 ID
- `to` 为用户的 `OpenID`

> 这里的 `-url` 是可选的，如果指定，则会在微信模板消息下部出现`详情`字样，可点击跳转

# 更多帮助

微信模板命令更多细节，请参看 `man weixin tmpl`