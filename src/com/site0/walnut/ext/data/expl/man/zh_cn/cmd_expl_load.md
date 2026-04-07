# 过滤器简介

`@load` 读取表达式内容


# 用法

```bash
expl @load  
  [-f /path/to]      # 内容来自文件
  ["expression"]     # 表达式内容
```

# 示例

```bash
# 从文件里获得
expl @load -f ~/myexpl.txt
# 从参数里获得
expl @load '=var_name?default_value'
# 从管道获得
cat ~/myexpl.txt | expl @load
```
