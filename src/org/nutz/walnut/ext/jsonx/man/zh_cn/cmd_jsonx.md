命令简介
======= 

`jsonx` 用来是一个处理 JSON 的过滤器型命令
    

用法
=======

```
jsonx [options] [[filter filter-args...]...]
```

它支持的`options`有

```bash
-cqn       # JSON 格式化参数
```

它支持的过滤器有：

```bash
read       # 从一个或多个文件读取JSON内容
map2list   # 将一个 map 变成 list
akeys      # 设置全局 JSON 输出的键白名单
ikeys      # 设置全局 JSON 输出的键黑名单
```

本命一开始会自动从标准输入读取 JSON　内容作为初始内容。