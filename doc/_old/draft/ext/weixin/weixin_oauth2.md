---
title:微信的oauth2验证
author:zozoh
tags:
- 系统
- 扩展
- 微信
- oauth2
---

# 微信 oauth2 的基本流程

```
C : 客户端
W : Walnut 服务
S : 微信服务

# 具体流程
1. C -> W : 发起 HTTP 请求  # /api/weixin/oauth2
2. W -> C : 生成重定向链接   # weixin xxx oauth2 "http://xxx" 
3. C -> S : 发送请求
4. S -> C : 验证通过，返回步骤 2 指定的 URL，同时带上参数 code
5. C -> W : 客户端通过code请求用户信息 # /api/weixin/oauth2_code
6. W -> S : 询问用户信息     # weixin xxx user -code xxx -infol follower
7. W -> C : 输出用户信息的JSON
```

# 附录

参考文章:

* [网页授权获取用户基本信息](http://mp.weixin.qq.com/wiki/4/9ac2e7b1f1d22e9e57260f6553822520.html)

