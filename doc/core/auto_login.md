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

	{
		ackey : "efg...opq"
	}
	
# 签名所需要的参数

1. user 字符串,对接的用户名,必须小写
2. once 字符串,一次性随机值,推荐用uu32或uu16格式,必须小写
3. time 长整型,时间戳, 精确到毫秒,有效期30分钟,请确保两端服务器的时间一致.

# 签名算法

	摘要算法 sha1
	
	sha1(ackey + "," + user + "," + time + "," + once)

# 签名示例

	ackey = "33khmpoe9cjhno207gis249fau"
	user = "wendal"
	time = 1456453615904
	once = "s3ppai29qkjucodatvdcptn4do"
	
	sha1("33khmpoe9cjhno207gis249fau,wendal,1456453615904,s3ppai29qkjucodatvdcptn4do")
	
	sign == 037b38e82a207f24e45b812f6e8115ad81c4b10b
	
# 登陆URL

1. 路径 $server/u/login/auto
2. 必选参数, user,time,once,sign, 均为小写
3. 可选参数, target, 需要跳转的路径URI

# 登陆URL示例

	http://192.168.2.205:8080/u/login/auto?sign=037b38e82a207f24e45b812f6e8115ad81c4b10b&user=wendal&time=1456453615904&once=s3ppai29qkjucodatvdcptn4do
	
# 响应

1. 登陆成功, 若target参数存在,跳转之,否则跳转到首页
2. 登陆失败, 显示失败原因:

	// 用户未启用自动登陆,设置改用户的ackey
	// 密钥签名错误,检查签名算法
	// 时间戳已过期,一般原因是两端的服务器时间未同步
	








