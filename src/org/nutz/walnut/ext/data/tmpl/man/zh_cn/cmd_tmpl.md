# 命令简介

`tmpl` 用来渲染一个字符串模板
    

# 用法

```bash
tmpl [@load | @vars] @render
```

它支持的过滤器有：

```bash
@load        # 读取模板内容
@vars        # 设置上下文变量
@render      # 渲染上下文
```
