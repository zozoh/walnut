# 过滤器简介

`@ints` 生成一个生成随机多段整数，可以用来生成 IPV4，版本号等数据

# 用法

```bash
fake [N] @ints [$TMPL]
# 生成模板的语法:
192.168.{0-255}.{1-23}
```

# 示例

```bash
# 随机生成 3 个 IPV4 地址
demo> fake 3 @ints 192.168.{1-12}.{0-255}
192.168.9.41
192.168.3.132
192.168.8.12
```
