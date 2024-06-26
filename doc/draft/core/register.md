---
title:新用户注册流程
author:zozoh
---

# 微信注册/登录

```
U : 用户
S : 云服务
X : 微信服务

# 具体流程
1. U -> S : 请求登录页面  # /u/h/login.html
2. U -> S : 选择微信登录，会弹出微信二维码
3. U -> X : 扫描二维码
4. X -> S : 验证通过，通知服务器
5. S -> U : 创建会话 ID

# 补充信息
6. S -> U : 如果没有用户名，弹出用户名收集页面 # /u/h/rename.html
7. U -> S : 填写最新的用户名
8. S -> U : 改名完成，打开主界面 # /a/open
```

# 手机注册

```
U : 用户
S : 云服务

# 具体流程
1. U -> S : 请求注册页面  # /u/h/signup.html
3. U -> S : 提供手机号
4. S -> U : 下发验证码
5. U -> S : 发送手机号，验证码，以及密码
6. S -> U : 创建会话 ID

# 补充信息
7. S -> U : 如果没有用户名，进入用户名修改页面 # /u/h/rename.html
8. U -> S : 填写最新的用户名
9. S -> U : 改名完成，打开主界面 # /a/open
```

验证码部分 @see [验证码机制](vcode.md)

# 邮箱注册

```
U : 用户
S : 云服务
E : 邮件服务器

# 具体流程
1. U -> S : 请求注册页面  # /u/h/signup.html
3. U -> S : 提供邮箱，以及密码
4. S -> E : 发送邮件验证码
5. S -> U : 提示用户到邮箱收取邮件
6. U -> S : 点击激活链接，或者输入验证码
7. S -> U : 创建会话 ID

# 补充信息
8. S -> U : 如果没有用户名，进入用户名修改页面 # /u/h/rename.html
9. U -> S : 填写最新的用户名
a. S -> U : 改名完成，打开主界面 # /a/open
```

验证码部分 @see [验证码机制](vcode.md)

# 用户名注册

```
U : 用户
S : 云服务

# 具体流程
1. U -> S : 请求注册页面  # /u/h/signup.html
2. U -> S : 提供登录名，以及密码
3. S -> U : 创建会话 ID

# 补充信息
3. S -> U : 进入用户设置界面 # /a/open/profile
4. U -> S : 可以自由选择进入手机，邮箱，微信绑定流程
```







