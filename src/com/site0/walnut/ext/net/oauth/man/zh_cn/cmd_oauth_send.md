命令简介
======= 

`oauth send` 命令用于生成第三方登录的URL

    
用法
=======

```    
oauth send [id]   # 第三方登录ID,例如github,qq
```

示例
=======

```
oauth send github https://nutz.cn/yvr/u/oauth/github/callback
https://github.com/login/oauth/authorize?client_id=30100863592971748671&response_type=code&redirect_uri=https%3A%2F%2Fnutz.cn%2Fyvr%2Fu%2Foauth%2Fgithub%2Fcallback&scope=user:email
```