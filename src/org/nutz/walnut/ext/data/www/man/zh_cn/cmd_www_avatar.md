# 命令简介 

`www avatar` 为某用户设置头像

# 用法

```bash
www avata
    [id:xxx]             # 【必须】站点主目录路径
    [UserName]           # 【选1】指定的用户
    [-ticket xxx]        # 【选1】给出以登录用户票据，这个更优先
    [-url 'http://..']   # 【必须】头像的地址。如果
                         # 用 `=` 开头表示从自身的一个键获取地址
    [-quiet]             # 【选】静默输出，默认输出用户的 JSON 信息
    [-ajax]              # 【选】是否输出为 ajax 格式
```

# 示例

```bash
# 从某个网址copy一个头像并设置给指定用户
demo@~$ www avatar -ticket 45y6..7e1a -url http://xxx

# 从某个网址copy一个头像并设置给指定用户
demo@~$ www avatar zozoh -url "=avatar"
```
