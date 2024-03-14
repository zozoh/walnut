# 命令简介 

`weixin user` 用来获取用户信息

# 用法

```bash
weixin {ConfName} user [..参数]
```

# 示例

```bash
# 根据 OpenID 获取用户信息
demo@~$ weixin xxx user -openid xxx

# 指定语言
demo@~$ weixin xxx user -openid xxx -lang zh_CN

# 根据 code 获取用户信息(仅OpenId)
demo@~$ weixin xxx user -code xxx

# 根据 code 获取用户信息(仅关注者)
demo@~$ weixin xxx user -code xxx -infol follower

# 根据 code 获取用户信息(任何人)
demo@~$ weixin xxx user -code xxx -infol others
```    