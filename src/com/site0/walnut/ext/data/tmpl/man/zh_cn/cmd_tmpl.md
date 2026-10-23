# 命令简介

`tmpl` 用来渲染一个字符串模板，支持占位符、循环、分支判断。

## 循环

```bash
# 上下文中有变量 alist 为集合或者数组
# 将循环渲染，每次循环，设置上下文 it 表示当前循环元素
# 同时支持 index 为当前元素下标（可选）
${#loop it,index : alist}
# 在这里可以用 ${index} 以及 ${it.xxx} 获取下标和循环元素
${#end}
```

## 分支判断

```bash
${#if "键":AutoMatch}
# 这里是分支渲染模板
${#else-if "xyz": AutoMatch}
# 这里是分支渲染模板
${#else}
# 这里是分支渲染模板
${#end}
```

# 用法

```bash
tmpl [-showkeys] @load [来源] @vars [变量JSON] @render
  -showkeys    # 开启后，占位符没有值时保留占位输出
```

支持的过滤器：

```bash
@load      # 读取模板内容（参数、-f 文件或管道）
@vars      # 设置上下文变量
@render    # 渲染并输出
```

# 示例

```bash
# 模板来自参数
tmpl @load '你好 ${name}' @vars '{name:"wendal"}' @render

# 模板来自文件
tmpl @load -f ~/tpl.txt @vars '{name:"wendal"}' @render

# 模板和变量都来自管道
echo '{name:"wendal"}' | tmpl @load -f ~/tpl.txt @vars @render

# 占位符无值时保留
tmpl -showkeys @load '你好 ${name}' @render
```
