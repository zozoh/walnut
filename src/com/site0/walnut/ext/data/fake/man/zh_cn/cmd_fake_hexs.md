# 过滤器简介

`@hexs` 生成多端随机整数(16进制)，可用来生成MAC地址，颜色值等

# 用法

```bash
fake [N] @hexs [$TMPL]
# 生成颜色
2R0:#{0-FF}{0-FF}{0-FF}

# 生成 Mac 地址
2R0:{0-FF}:{0-FF}:{0-FF}:{0-FF}:{0-FF}:{0-FF}
```

# 示例

```bash
# 随机生成 3 个 网卡地址
demo> fake 3 @hexs -upper 2R0:{0-FF}:{0-FF}:{0-FF}:{0-FF}:{0-FF}:{0-FF}
04:7C:DB:DB:11:0F
79:E7:1D:A7:42:43
D6:C4:EC:8E:DF:32

# 随机生成 3 个16进制颜色
demo> fake 3 @hexs -upper 2R0:#{0-FF}{0-FF}{0-FF}
#5F9197
#5A6E58
#04FF24
```
