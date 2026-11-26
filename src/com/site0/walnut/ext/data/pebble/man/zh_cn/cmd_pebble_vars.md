# 过滤器简介

`@vars` 设置渲染上下文变量。

变量是一个 JSON 对象，模板里用 `{{ 键 }}` 取值，嵌套对象用 `{{ a.b.c }}`。
多次使用时**与已有变量融合**（后给的覆盖同名的）。

# 用法

```bash
pebble @vars
  ["{x:100,y:90}"]   # 变量值来自参数，可以给多个，依次融合
  [-f /path/to]      # 变量值来自一个 JSON 文件
                     # 参数、文件都不给，则从管道读取
```

# 示例

```bash
# 从参数里获得
pebble @vars '{title:"首页",nav:{...}}'

# 从文件里获得
pebble @vars -f ~/tmp/vars.json

# 从管道获得（jsc 里最常用）
echo '{"title":"首页"}' | pebble @load -f ~/templates/views/home.html @vars @render
```
