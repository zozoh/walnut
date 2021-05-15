# 命令简介

`fake` 是用来生成一组模拟数据
    

# 用法

```bash
fake [N] [-cqn] [[@filter args...]...]
```

它支持的过滤器有：

```bash
@bean        # 生成一个或者多个模拟的对象
@name        # 生成随机姓名
@text        # 生成随机文本
@integer     # 生成随机整数
@ints        # 生成一个生成随机多段整数
@uu32        # 生成 UU32
@ams         # 生成随机绝对毫秒数
```
