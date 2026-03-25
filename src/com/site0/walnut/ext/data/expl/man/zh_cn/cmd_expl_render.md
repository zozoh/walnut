# 过滤器简介

`@render` 渲染表达式并输出结果


# 用法

```bash
expl @render
```

# 示例

```bash
# 从文件里获得表达式和变量
expl @load -f ~/myexpl.txt @vars -f ~/myvars.txt @render

# 从参数里获得表达式和变量
expl @load '=var_name' @vars '{"var_name":"Hello World"}' @render

# 使用布尔判断
expl @load '==is_active' @vars '{"is_active":true}' @render

# 使用默认值
expl @load '=undefined_var?default_value' @render
```
