# 过滤器简介

`@render` 渲染上下文


# 用法

```bash
tmpl @render  
```

# 示例

```bash
# 从文件里获得
tmpl @load -f ~/mytmpl.txt @vars -f ~/myvars.txt @vars  @render
# 从参数里获得
tmpl @load 'a${x}b' @vars '{x:100}' @render
# 从管道获得
cat ~/mytmpl.txt | tmpl @load @vars '{x:100}' @render
```
