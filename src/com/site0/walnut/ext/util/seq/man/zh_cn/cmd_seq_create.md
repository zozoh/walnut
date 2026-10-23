# 过滤器简介

`@create` 根据模板生成序列。

两种模式：

- **简单序列模式**（默认）：按 `-start`/`-step`/`-count` 渲染模板，逗号分隔输出
- **列表模式**（给 `-match`）：查询一组对象，逐个渲染序列值并存入对象的 `-key` 字段

模板里的变量：

```bash
${seq}        # 当前序列值
${obj.u_xxx}  # 列表模式下，当前对象的字段值
${变量名}     # -vars 传入的自定义变量
```

# 用法

```bash
seq @create -tmpl 模板 [-start N] [-step N] [-count N] [-match JSON] [-sort 排序] [-key 键] [-force] [-vars JSON] [-cqn]
  -tmpl     # 渲染模板（必填），如 '${seq<int:%03d>}'
  -start    # 起始值，默认 1
  -step     # 步进，默认 1
  -count    # 简单模式下生成个数，默认 100
  -match    # 列表模式：对象查询条件 JSON，如 '{pid:"xxx"}'
  -sort     # 列表模式下对象的排序，默认 ct:1
  -key      # 列表模式下存储序列值的键，默认 u_code
  -force    # 覆盖对象已有的序列值，默认跳过
  -vars     # 额外变量 JSON；'-vars ~pipe' 或 '-vars true' 表示从管道读
  -cqn      # 输出 JSON 的格式化开关
```

# 示例

```bash
# 简单数值序列
seq @create -tmpl '${seq}'

# 固定长度 3 的序列，只生成 5 个
seq @create -tmpl '${seq<int:%03d>}' -count 5

# 带自定义变量
seq @create -tmpl '${prefix}${seq<int:%03d>}' -vars '{prefix:"A"}' -count 3

# 列表模式：给一组选手生成赛号，存入 u_code
seq @create -tmpl 'A${seq<int:%03d>}${obj.u_sex}' -match '{pid:"48mv1tu9a2j5arrcgiiub6slhu",u_pj:"10KM"}'

# 变量从管道读
echo '{prefix:"B"}' | seq @create -tmpl '${prefix}${seq}' -vars ~pipe -count 3
```
