# 过滤器简介

`@load` 读取模板内容


# 用法

```bash
tmpl @load  
  [-f /path/to]      # 内容来自文件
  ["xxx${x}xxx"]     # 模板内容
  [-c '<>']          # 前后界定字符，
                     # 如果是 '<>' 表示用 '$<xxx>' 来声明占位符
                     # 如果是 '{}' 表示用 '${xxx}' 来声明占位符
                     # 默认 '<>'
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
