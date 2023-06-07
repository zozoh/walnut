# 命令简介

`tmpl` 用来渲染一个字符串模板。

支持

- 占位符
- 循环
- 分支判断

模板具体语法如下：

## 循环

```bash
# 上下文中有变量 alist 为集合或者数组
# 将循环渲染，每次循环，设置上下文 it 表示当前循环元素
# 同时支持 index 为当前元素下标（可选），
$<loop @ it,index : alist >
#
# 在这里可以用 ${index} 以及 ${it.xxx} 获取下标和循环元素
#
$<end>
```

# 分支判断

```bash
$<if @ AutoMatch>
# 这里是分支渲染模板
${else-if @ xyz: AutoMatch}
# 这里是分支渲染模板
${end}
```
    

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
