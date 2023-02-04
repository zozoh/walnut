# 过滤器简介

`@load` 读取模板内容


# 用法

```bash
tmpl @vars  
  [-f /path/to]      # 内容来自文件
  ["xxx${x}xxx"]     # 模板内容
  [-reset]           # 开启这个开关将会重置上下文变量
                     # 默认的要与上下文融合
```

# 示例

```bash
# 从文件里获得
tmpl @load -f ~/mytmpl.txt
# 从参数里获得
tmpl @load 'a${x}b'
# 从管道获得
cat ~/mytmpl.txt | tmpl @load
```
