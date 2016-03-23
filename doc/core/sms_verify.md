---
title:短信验证
author:zozoh
---

# 设计动机

系统有两种账号:

* walnut 的系统账号
* 一般用户的 `~/.usr` 账号

无论哪种账号，都可能需要验证手机号。本文将给出验证的详细过程。

* 系统账号，修改的是 */sys/usr/xxx* 的内容
* 一般用户，修改的是 *~/.usr/xxx* 的内容

# 账号的验证

* 短息的发送将采用 `sms` 命令在 *root* 用户下的配置
* 短息发送 API 将采用 *root* 用户下的 *.regapi*

```
C : 客户端
W : Walnut服务
S : 短息服务

# 调用顺序
C -> W : /api/sms/vcode_get
W -> W : env scode=`random -s 6`
W -> W : obj id:${uid} 
            -u 'phone:"${phone}"
                sms_vcode:"$scode", 
                sms_vnext:"%ms:now+1m"'
                sms_vexpi:"%ms:now+10m"'
W -> W : sms -r ${phone} "您的验证码是 $scode"
```

当客户得到短信，回输验证码

```
C : 客户端
W : Walnut服务
S : 短息服务

# 调用顺序
C -> W : /api/sms/vcode_check
W -> W : 检查传入的 vcode 是否相符，如果相符
W -> W : obj id:xxxxx  
            -u 'sms_vcode:null,
                sms_vexpi:null,
                sms_vnext:null,
                phone_verified:true'
```

# 系统用户的更多操作

对于 `/api/sms/vcode_get`，还需要保存用户的内容

```
cat id:${uid}
| json -u 'phone:"${phone}"'
> id:${uid}
```

对于 `/api/sms/vcode_check`，也需要保存用户的内容

```
cat id:${uid}
| json -u 'phone_verified:true'
> id:${uid}
```


