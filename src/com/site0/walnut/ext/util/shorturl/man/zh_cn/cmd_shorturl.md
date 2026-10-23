# 命令简介

`shorturl` 把长网址转成短网址（新浪短网址服务）

不带参数时输出本说明。

# 用法

```bash
shorturl [-h] URL
  -h    # 去掉 http:// 前缀
```

# 示例

```bash
# 生成短网址
shorturl http://nutzam.com/index.html

# 不带协议前缀
shorturl -h http://nutzam.com/index.html
```
