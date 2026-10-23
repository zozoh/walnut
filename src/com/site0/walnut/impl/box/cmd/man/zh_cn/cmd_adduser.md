# 命令简介

`adduser` 添加一个用户，并做好对应的初始化工作

通常需要 root 组管理员权限（要读写 `/sys/usr`）。用户已存在时默认报错，
加 `-exists` 则直接输出已存在的用户。

# 用法

```bash
adduser [-p 密码|-p] [-setup 脚本] [-m 模式] [-exists] [-json|-out 模板] [-cqnN] 用户
  -p        # 设置密码；只写 -p 等价于密码 123456；不指定则无密码
  -setup    # 用户创建后执行的设置脚本目录（会调用 setup 命令）
  -m        # 用户初始化类型，影响 setup 行为，同时给用户增加 init 字段
  -exists   # 用户已存在时不报错，直接输出
  -json     # 以 JSON 输出用户完整信息（隐藏 passwd/salt）
  -out      # 以模板输出，占位符形式 @{xxx}
  -cqnN     # JSON 格式开关；N 表示结尾不输出换行
用户        # email / 手机 / 用户名，或 oauth:github:profileId、wxgh:公众号:openid
```

# 示例

```bash
# 添加一个用户
adduser xiaobai

# 添加一个微信公号用户并初始化
adduser wxgh:gh_7a167f4843b6:okyrt3iQ875fTF96BEtqLrZc345V -setup usr/create

# 添加一个用户，如果存在，就不创建，仅仅查询
adduser -exists xiaobai
```
