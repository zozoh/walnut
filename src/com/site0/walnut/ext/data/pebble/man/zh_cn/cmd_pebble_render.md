# 过滤器简介

`@render` 渲染模板并输出到标准输出。

要落盘就接 shell 重定向（目标不存在会自动创建）：

```bash
pebble @load -f ~/templates/views/x.html @vars -f ~/tmp/v.json @render > ~/www/website/x.html
```

# 用法

```bash
pebble @render [-strict] [-maxsize 8388608]
  -strict        # 变量未定义时报错（默认关，未定义渲染为空串）
                 # 模板一多，变量名拼错会静默变空，发布前建议开一次
  -maxsize <n>   # 单次渲染输出上限（字节），默认 8388608，超了报错
```

# 示例

```bash
# 输出到终端
pebble @load -f ~/templates/views/x.html @vars -f ~/tmp/v.json @render

# 输出到文件
pebble @load -f ~/templates/views/x.html @vars -f ~/tmp/v.json @render > ~/out.html

# 严格模式 + 放宽上限
pebble @load -f ~/templates/views/x.html @vars -f ~/tmp/v.json @render -strict -maxsize 16777216
```
