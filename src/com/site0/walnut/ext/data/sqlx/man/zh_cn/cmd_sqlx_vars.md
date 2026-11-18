# 过滤器简介

`@vars` 为本次 `sqlx` 命令设置变量集合。变量供模板里的 `${...}` 和 `${@vars=...}`
使用；**模板语法不在本篇**，见
[编写 SQL 模板（sqlx）](@wn-doc::reference/101.dev/21-write-sqlx-tmpl.md)。

# 用法

```bash
sqlx @vars
  [{Vars}...]        # JSON/JSON5 对象；多个对象会合并
                     # 不写则从标准输入读取
  [-reset]           # 先清空已有变量集合
  [-merge]           # 与已有变量深度合并；默认是替换
  [-as auto|list|map]# 变量模式，默认按输入自动判断
  [-omit k1,k2]      # 输入变量中排除这些键
  [-pick k1,k2]      # 输入变量中只保留这些键
  [-put expr]        # 从管道上下文取值，写入当前对象
  [-explain]         # 展开变量里的时间等宏
  [-view]            # 输出当前变量集合
  [-fake spec]       # 生成测试对象，如 '10:/path/to/bean.json'
  [-lang zh_cn]      # -fake 的模拟数据语言，默认 zh_cn
```

## 输入来源

```bash
# 直接写 JSON5
sqlx @vars '{name:"Be", age:12}'

# 从标准输入
cat ~/vars.json | sqlx @vars

# 从输入/管道上下文取一个键
sqlx @vars '=pet'

# 把输入列表整体拿进来
sqlx @vars '=..'
```

- 位置参数以 `=` 或 `:` 开头时，按“上下文键”解释
- `=..` 表示把当前输入整体并入
- 不写位置参数且没有标准输入时，使用当前上下文

## `-as` 的模式

- `auto`：默认；输入是数组/集合用 `list`，否则 `map`
- `list`：批量模式，后续 `@exec` 会按列表逐条执行
- `map`：单对象模式

批量插入/更新通常要显式 `-as list`，避免输入被当成单个 Map。

## `-merge` 与 `-reset`

- 默认：新变量集合**替换**当前 Map（`assign`）
- `-merge`：深度合并到当前 Map
- `-reset`：先清空 Map 和 List，再设置

## `-put`

从管道上下文取值补到当前对象；常用于“前一条 SQL 查出的 id 给下一条插入用”。

赋值表达式是 `key=path`，并可用 `?` / `!` 标记“只在对象没值时设置 / 强制设置”。
细节见 `SqlVarsPutting`。

## `-fake`

```bash
sqlx @vars -fake '10:~/fake_bean.json' -as list @exec pet.insert
```

- `10:/path`：生成 10 个对象
- `/path`：生成 1 个对象
- `-lang` 只影响 fake 数据

# 示例

```bash
# 单对象查询
sqlx @vars '{name:"Be", age:12}' @query pet.select

# 批量插入
cat ~/pets.json | sqlx @vars -as list @exec pet.insert

# 只保留两个字段
sqlx @vars '{name:"Be", age:12, city:"X"}' -pick name,age @query pet.select
```

模板里的 `@vars=where`、`@vars=param`、别名 OR 等写法见 21 篇；不要把模板语法
抄进命令参数里。
