# 命令简介

`json` 将管道输入的 JSON 文本加工后再输出

加工动作按固定顺序执行：解析（`-type`）→ 容错（`-err`）→ 取值（`-get`/`-val`）
→ 过滤键（`-key`）→ 映射（`-mapping`）→ 加前缀（`-prefix`）→ 修改（`-u`/`-a`）
→ 打包（`-put`）→ 输出（JSON / `-out` 模板 / `-str`）。是 regapi 里组装
响应数据的核心工具。

# 用法

```bash
[上游] | json [-cqnr] [-type 类型] [-err] [-get 路径] [-val 键] [-key 正则]
        [-mapping '{映射}'] [-mapping_only] [-prefix 前缀] [-prefix_key 正则]
        [-u '{更新}'] [-a '{默认值}'] [-put 键] [-keys] [-e 正则] [-d 日期格式]
        [-out 模板] [-str]
  -cqn        # JSON 格式开关：-c 紧凑、-q 键名加引号、-n 输出 null 值
  -type       # 输入类型：String|Integer|Float|Long|Double|Boolean|Object（默认）
  -err        # 输入为空或以 e. 开头（错误串）时原样输出，不抛解析错误
  -get        # 按路径取值，支持 . 与 [下标]，如 pos.x / 0.pos.x / obj[0].x
  -val        # 取对象的键值；输入是数组时生成值数组
  -key        # 只保留匹配正则的键；! 开头表示反选
  -mapping    # 映射字段键值，如 '{a:"AA", b:"BB:@{a}-@{b}"}'；值部分支持 @{x} 模板
  -mapping_only   # 只保留 mapping 中声明的字段（需配合 -mapping）
  -prefix     # 给键名加前缀，如 -prefix "pos_"
  -prefix_key # 只处理匹配正则的键；! 开头表示反选
  -r          # -prefix 递归处理内层对象
  -u          # 深合并更新（已存在的键覆盖）
  -a          # 深合并更新（只补充不存在的键）
  -put        # 把整个对象包到指定键下，如 -put pos
  -keys       # 输出对象的键数组
  -e          # 只输出匹配正则的字段；! 开头表示排除
  -d          # 日期字段的格式化模板，如 yyyy-MM-dd
  -out        # 按 @{xxx} 模板输出（集合则逐元素渲染）
  -str        # 按字符串输出（否则转 JSON）
```

# 示例

```bash
# 格式化
echo '{x:100,y:80}' | json

# 增加前缀
echo '{x:100,y:80}' | json -prefix "pos_"

# 只为指定字段加前缀
echo '{x:100,y:80}' | json -prefix "pos_" -prefix_key "^x$"

# 打包成新 map
echo '{x:100,y:80}' | json -put "pos"

# 取值后打包
echo '{pos:{x:100,y:80}}' | json -get "pos" -put "position"

# 过滤键（反选）
echo '{x:100,y:80,z:66}' | json -key "!^(x|y)$"

# 深取值
echo '{pos : {x:100,y:80}}' | json -get "pos.x"

# 列表深取值
echo '[{pos : {x:100,y:80}},{}]' | json -get "0.pos.x"

# 数组提取字段为值数组
echo '[{x:1,y:2},{x:9,y:8}]' | json -val x

# 字段映射改名
echo '{a:"pet",b:"good"}' | json -mapping '{a:"AA",b:"BB:@{a}-@{b}"}' -mapping_only

# 错误串原样透传
echo 'e.io.noexist' | json -err

# 模板输出
echo '{x:100,y:80}' | json -out 'X=@{x} Y=@{y}'
```
