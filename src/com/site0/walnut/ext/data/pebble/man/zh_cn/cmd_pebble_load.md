# 过滤器简介

`@load` 读取模板内容。

模板就是 Walnut 对象树里的一个文件，用绝对路径（`~/xxx`）指过去即可。

# 用法

```bash
pebble @load
  [-f /path/to]      # 内容来自文件：~/xxx、/home/xxx、或相对当前目录
  ["模板内容"]        # 模板源码直接给
                     # 两者都不给，则从管道读取
```

# 示例

```bash
# 从文件里获得
pebble @load -f ~/templates/views/home.html

# 从参数里获得
pebble @load '<h1>{{ title }}</h1>'

# 从管道获得
cat ~/templates/views/home.html | pebble @load
```

模板里的 `extends` / `include` / `import` 也走同一套路径：`~/xxx` 是绝对路径，
其余相对**当前模板所在目录**解析（详见 `man pebble`）。
