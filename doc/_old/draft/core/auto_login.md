---
title:自动登陆
author:wendal
tags:
- 系统
- 会话
---

# 应用场景

与第三方系统进行session对接,免除用户的登陆过程

# 用户密钥

存在在用户配置文件内

```
{
   // 自动登录的秘钥，请妥善保存，定期更换，防止泄露
	ackey : "efg...opq", 
	// 链接超时秒数,默认半小时(1800秒),设置为0代表禁用自动登陆, 设置为-1为永不超时
	ackey_timeout : 1800
}
```

这两个值是怎么设置进去的呢？ walnut 提供了命令

```
usermod -E '{"ackey":"`uuid`", "ackey_timeout":1800}'
```

* 关于 `usermod` 命令的更多用法，请执行 `man usermod` 查看帮助文档
	
# 签名所需要的参数

1. *user* 字符串,对接的用户名,必须小写
2. *once* 字符串,一次性随机值,推荐用uu32或uu16格式,必须小写
3. *time* 长整型,时间戳, 精确到毫秒,请确保两端服务器的时间一致.

# 签名算法

```
摘要算法 sha1
	
sha1(ackey + "," + user + "," + time + "," + once)
```

# 签名示例

```
比如，你的信息是:

ackey = "33khmpoe9cjhno207gis249fau"
user  = "wendal"
time  = 1456453615904
once  = "s3ppai29qkjucodatvdcptn4do"

那么你应该这么做签名：

SHA1("33khmpoe9cjhno207gis249fau,wendal,1456453615904,s3ppai29qkjucodatvdcptn4do")

得到的结果是:
sign == 037b38e82a207f24e45b812f6e8115ad81c4b10b
```

那么拿到这个签名，你应该怎么使用呢？ 请往下看

# 登陆URL

通常你会发送一个 URL 到服务器，这个 URL 的结构为

```
http://服务地址:端口/u/do/login/auto?user=用户&time=本地时间&once=你的随机数&sign=你的签名&target=你要跳转的链接地址
```

1. 路径 $server/u/do/login/auto
2. 必选参数, user,time,once,sign, 均为小写
3. 可选参数, target, 需要跳转的路径URI

比如，上例，你的请求应该是

```
http://192.168.2.205:8080/u/do/login/auto?sign=037b38e82a207f24e45b812f6e8115ad81c4b10b&user=wendal&time=1456453615904&once=s3ppai29qkjucodatvdcptn4do
```
	
# 响应

1. 登陆成功, 若target参数存在,跳转之,否则跳转到首页
2. 登陆失败, 显示失败原因:
    - 用户未启用自动登陆,设置改用户的ackey
	 - 密钥签名错误,检查签名算法
	 - 时间戳已过期,一般原因是两端的服务器时间未同步

# 生成签名的代码实例:Java

```java
package org.nutz.walnut.tool;

import org.nutz.lang.Lang;
import org.nutz.lang.random.R;

public class GenAutoLoginUrl {

    public static void main(String[] args) {
        // .............................................
        // 计算签名前，你需要获得的信息

        // 签名的秘钥
        String ackey = "33khmpoe9cjhno207gis249fau";

        // 用户名
        String user = "wendal";

        // 请求者时间
        long time = System.currentTimeMillis();

        // 随机字符串
        String once = R.UU64();

        // .............................................
        // 拼合成要签名字符串
        String[] ss = Lang.array(ackey, user, time + "", once);
        String strForSign = Lang.concat(",", ss).toString();

        // .............................................
        // 生成签名
        String sign = Lang.sha1(strForSign);

        // .............................................
        // 输出签名的链接地址
        String url = "http://192.168.2.205:8080/u/do/login/auto";
        url += "?sign=" + sign;
        url += "&user=" + user;
        url += "&time=" + time;
        url += "&once=" + once;

        // .. 并打印
        System.out.println(url);

    }

}
```



