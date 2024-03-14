# 命令简介 

`weixin info` 用来显示指定公众号的配置信息

# 用法

```bash
weixin {ConfName} info [正则表达式]
```

# 示例

```bash
# 显示全部配置信息
demo@~$ weixin xxx info

# 显示被正则表达式约束的配置信息
demo@~$ weixin xxx info "^pay_.+$"
```