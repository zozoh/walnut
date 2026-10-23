# 命令简介

`seq` 序列生成器：为一组对象生成序列值，或直接渲染一段序列文本。

# 用法

```bash
seq @create [参数...]
```

支持的处理器：

```bash
@create    # 生成序列
```

# 序列生成规则

模板用 Tmpl 语法渲染，可用的变量：

```bash
${seq}                 # 当前序列值，可设置开始值与步进
${obj.u_xxx}           # 列表模式下，当前 WnObj 的字段值
${自定义变量}          # 由 -vars 传入的其他变量
```

# 示例

```bash
# 简单数值序列，从 1 开始，输出 100 个
seq @create -tmpl '${seq}'

# 固定长度 3 的数值序列
seq @create -tmpl '${seq<int:%03d>}'

# 列表模式：给一组对象生成序列并存入 u_code 字段
seq @create -tmpl '${seq<int:%03d>}' -match '{tp:"thing"}' -key u_code

# 带自定义变量
seq @create -tmpl '${prefix}${seq<int:%03d>}' -vars '{prefix:"A"}' -count 5
```
