# 命令简介

`httpparam` 将 HTTP 请求参数解析成 JSON 或模板输出

是 regapi 体系里解析请求的第一环：输入为请求对象文件（`-in`）或管道内容，
自动按 JSON 或 form 表单解析，并合并请求对象上的 `http-qs-*` 查询参数。
之后可按需做类型转换、收缩到子 Map、模板输出。

# 用法

```bash
httpparam (-in 请求对象 | 管道) [-cqn] [-out 模板] [-map [键]]
          [-varray 名:分隔符,...] [-vint 名,...] [-vbool 名,...] [-vfloat 名,...]
  -in      # 请求对象（如 id:xxx），缺省读管道
  -out     # 输出模板（@{xxx} 占位符），缺省输出 JSON
  -map     # 收缩到子 Map：-map 键为 params，-map REQ 键为 REQ
  -varray  # 按分隔符拆分成数组，如 -varray 'ids:,' 
  -vint    # 转成整数，如 -vint 'a,b'
  -vbool   # 转成布尔
  -vfloat  # 转成浮点数
  -cqn     # JSON 格式开关：-c 紧凑、-q 键名加引号、-n 输出 null 值
```

# 示例

```bash
# 输出请求中的 id 字段
httpparam -in id:xxx -out 'ID is @{id}'

# 请求参数转 JSON（紧凑）
httpparam -in id:xxx -c

# 类型转换：price 转浮点，d 和 a 转布尔
httpparam -in id:xxx -vfloat price -vbool 'd,a'

# 拆分数组：ids=a,b,c 变成数组
httpparam -in id:xxx -varray 'ids:,'

# 收缩为 {params:{...}}
httpparam -in id:xxx -map

# 收缩为 {REQ:{...}}
httpparam -in id:xxx -map REQ
```
