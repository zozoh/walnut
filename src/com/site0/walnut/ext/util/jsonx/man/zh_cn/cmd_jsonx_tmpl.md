# 过滤器简介

`@tmpl` 将上下文中的 JSON 对象按照字符串模板渲染输出

# 用法

```bash
@tmpl [TMPL1 ...]  # 多个值为多个模板
```

# 示例

```bash
jsonx '{x:100,y:99}' @tmpl "x=${x} and y=${y}"
# > x=100 and y=99
```

