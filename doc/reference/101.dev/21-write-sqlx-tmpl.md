---
title: 编写 SQL 模板（sqlx）
module: 101.dev
keywords: [sqlx, SQL 模板, @vars, 动态 SQL, where, SqlEntry, 条件值, 批量, batch]
source:
  - ext/data/sqlx/loader/SqlEntry.java
  - ext/data/sqlx/tmpl/WnSqlTmpl.java
  - ext/data/sqlx/tmpl/WnSqlElementMaker.java
  - ext/data/sqlx/tmpl/vars/SqlVarsElement.java
  - ext/data/sqlx/ast/SqlCriteriaNode.java
related:
  - ../../../src/com/site0/walnut/ext/data/sqlx/man/zh_cn/cmd_sqlx.md
---

# 编写 SQL 模板概述

业务 SQL 最难的从来不是语句本身，而是 `WHERE` 子句的动态组合：
字段可能传、可能不传，传了可能还要区间、模糊、排序……
用字符串拼接不但丑，还容易引入注入漏洞。

`sqlx` 把 SQL 写成**模板文件**：固定的部分原样书写，
动态的部分用占位符声明，渲染时根据传入的参数决定输出什么。
渲染有两种模式：

- **预编译模式**：动态值输出为 `?`，同时收集参数列表，交给 `PreparedStatement`
- **文本模式**：动态值直接转义拼接为字面量（调试用）

下面这个例子一眼就能看懂它的工作方式：

```sql
-- @name = pet.select
-- @type = SELECT
SELECT id, name, age FROM t_pet WHERE 1=1
${@vars=where;prefix=AND}
${@vars=order;dft=id ASC}
```

```bash
# 传入 name 和 age，只拼接有值的条件
sqlx @query pet.select -vars '{name:"Be", age:[1,12]}'
# 输出: SELECT id, name, age FROM t_pet WHERE 1=1 AND name='Be' AND (age>=1 AND age<=12) ORDER BY id ASC

# 不传任何条件，WHERE 为空，落到默认排序
sqlx @query pet.select -vars '{}'
# 输出: SELECT id, name, age FROM t_pet WHERE 1=1 ORDER BY id ASC
```

命令的用法（`@query` `@exec` `@vars` `@view` 等子命令）参见
`cmd_sqlx` 的 man 文档，本篇只讲**模板怎么写**。

## 模板文件的结构

一个 `.sql` 文件可以包含**多个** SQL 实体，每个实体由一组 `--` 注释行开头：

```sql
-- @name = pet.select
-- @type = SELECT
-- @pick = id name age
-- @ignoreNil = true
SELECT id, name, age FROM t_pet WHERE 1=1
${@vars=where;prefix=AND}

-- @name = pet.update
-- @type = UPDATE
UPDATE t_pet SET ${@vars=update} WHERE id=${id}
```

解析规则（参见 `SqlEntry.java`）：

- 注释行的内容须匹配 `@key = value`（`--` 之后的内容会被去掉两头空白再匹配）
- 已知的键只有 5 个，其余的键会被静默忽略：
  - `@name`：实体名，**必填**，也是调用时的标识（如 `pet.select`）
  - `@type`：`SELECT` / `UPDATE` / `DELETE` / `INSERT` / `EXEC`，选填，主要给执行器做提示
  - `@pick`：空格分隔的字段名，作为模板里所有 `@vars` 元素的默认 pick
  - `@omit`：同上，默认 omit
  - `@ignoreNil`：`true`/`false`，默认忽略 `null` 值字段
- 注释行之后的普通行，按空格拼接为模板内容（所以模板写成多行也没问题）
- 下一个 `--` 注释行开始时，推入上一个实体

实体级的 `pick`/`omit`/`ignoreNil` 是模板里每个 `@vars` 元素的默认值：
元素自己没有声明时才生效。

## 占位符

模板里有两类占位符。

### 普通变量 `${var}`

就是 `WnTmplX` 的标准变量替换，值会被直接转义拼进 SQL：

```sql
-- @name = pet.get
SELECT * FROM t_pet WHERE id=${id}
```

适合值必然存在、且不参与动态组装的场景。

### 动态占位符 `${@vars=类型;配置}`

`@vars` 开头的占位符由 `WnSqlElementMaker` 创建，类型决定它渲染成什么。
配置项用 `;` 分隔，格式为 `键=值`，旗标项（如 `ignoreNil`）只写键名：

```
${@vars=where;prefix=AND;ignoreNil}
```

类型一共有 6 种，下面逐一介绍。

## 六种 @vars 类型

### `param`：单个参数

输出一个 `?`（预编译模式）或一个字面量（文本模式）。
用于 `@vars` 体系之外的零散参数，尤其是 `LIKE` 这种需要包一层格式的场景：

```sql
-- @name = pet.search
SELECT * FROM t_pet WHERE name LIKE ${@vars=param;name=hint;format=%$[hint]%}
```

- `name=hint`：从上下文中取 `hint` 键的值
- `format=%$[hint]%`：值的格式化模板，格式里用 `$[键]` 取上下文值（
  注意格式模板的分隔符是 `$[...]`，与主模板的 `${...}` 不同），
  `format` 优先级高于 `name`

### `where`：动态条件（核心）

把上下文（或 `scope` 指定的子树）渲染为条件表达式：

```sql
SELECT * FROM t_pet WHERE 1=1 ${@vars=where;prefix=AND}
```

**默认组合规则只有两条，记住就够了**：

- 传入**对象**：每个键是一条条件，键与键之间是 **AND**
- 传入**数组**：每个元素是一条条件，元素与元素之间是 **OR**

```js
// ① 对象：AND
{name:"Be", age:3}
→ name='Be' AND age=3

// ② 数组：OR
[{name:"Be"}, {name:"Ka"}]
→ name='Be' OR name='Ka'

// ③ 对象里的数组值：IN（不是 OR）
{name:["Be","Ka"]}
→ name IN ('Be','Ka')

// ④ 数组里的对象元素：每个元素先内部 AND，再与其他元素 OR
[{tp:"dog", age:[1,12]}, {tp:"cat", age:1}]
→ ((tp='dog' AND (age>=1 AND age<=12)) OR (tp='cat' AND age=1))

// ⑤ 数组元素也可以是单值条件（Str/范围串等写法同样适用）
[{ct:"[2025,]"}, {"!ct":"[2026,]"}]
→ ct>='2025' OR NOT ct>='2026'
```

再叠加 `$or` / `$and` 组合键与 `!` 取反，就能拼出任意复杂的 WHERE：

```js
{tp:"dog", $or:[{age:1}, {age:2}]}
→ tp='dog' AND (age=1 OR age=2)

{"!tp":"dog", age:[1,9]}
→ NOT tp='dog' AND (age>=1 AND age<=9)
```

其他行为：

- 上下文为空（或所有字段被 pick/omit/ignoreNil 过滤完）时输出空字符串
- `prefix` 在条件非空时输出，前后自动带空格（`prefix=AND` → ` AND `），
  所以模板上写 `WHERE 1=1 ${@vars=where;prefix=AND}` 即可，不用操心前面留不留空格
- 还可以用 `dft` 指定上下文为空时的兜底 SQL
- 值的具体写法（区间、模糊、正则、null……）见下一节

### `order`：动态排序

把上下文的键值对渲染为排序列表，**值是数字，规则与 MongoDB 的排序写法一致**：
`1` 升序、`-1` 降序：

```sql
SELECT * FROM t_pet ORDER BY ${@vars=order;dft=id ASC}
```

```js
// 传入的排序参数
{sort: {name: 1, ct: -1}}   // 渲染为: ORDER BY name ASC, ct DESC
```

- 键是字段名，值是 `1` / `-1`，**不要写成 `"asc"` / `"desc"` 字符串**
  （当前实现虽对 `"desc"` 字符串有兼容判断，但属于未文档化的兼容行为，
  请统一用数字写法）
- `__`、`$` 开头的键被跳过（上下文隐藏字段）
- `dft` 指定没有任何排序字段时的兜底 SQL
- `fpref` 给字段名加前缀，联合查询时好用

> ⚠️ 当前版本的 `prefix` 配置在 `order` 类型上不生效
> （`VarsAsOrderByElement` 在填充排序列表之前就判断了列表是否为空，
> 前缀永远不会输出）。请把关键字直接写死在模板里：
> `SELECT * FROM t_pet ORDER BY ${@vars=order;dft=id ASC}`

### `update`：动态赋值

把上下文的键值对渲染为 `key=?, key=?` 列表，用于 `UPDATE ... SET`：

```sql
-- @name = pet.update
UPDATE t_pet SET ${@vars=update} WHERE id=${id}
```

- 值以 `:=>` 开头表示**原样 SQL**，直接拼接不做参数化：
  `{age:":=>age+1"}` 渲染为 `age=age+1`
- 其余值在预编译模式下输出 `?` 并收集参数

### `insert.columns` / `insert.values`：动态插入

两者分别渲染字段名列表和值列表，配成一对使用：

```sql
-- @name = pet.insert
INSERT INTO t_pet (${@vars=insert.columns}) VALUES (${@vars=insert.values})
```

上下文 `{name:"Be", age:3}` 渲染为
`INSERT INTO t_pet (name, age) VALUES (?, ?)`。

## 配置项全集

所有 `@vars` 类型共用的配置（定义在 `SqlVarsElement` 构造器）：

- `scope=子树键名`：不从整个上下文取数，而取上下文中该键对应的子 Map
- `pick=a,b,c`：只取这几个键
- `omit=a,b`：排除这几个键
- `ignoreNil`：忽略值为 `null` 的键（旗标，不写 `=值`）
- `dft=SQL片段`：没有可渲染内容时的兜底输出（各类型细节见上）
- `prefix=关键字`：整体前缀（where 有效，order 当前无效，update 直接拼接在头部）
- `mapping=a:A1;b:B1`：键名映射，把上下文键 `a` 改名成 `A1` 再渲染
- `fpref=前缀`：给输出的字段名加前缀，如 `fpref=t_.` → `t_.name`
- `alias_or=a:b|c,x:y|z`：别名 OR 组，详见下文 where 的说明

`param` 类型额外支持：

- `name=键路径`：取值键名，支持 `a.b` 路径
- `format=模板`：格式化模板，`$[键]` 取值，优先级高于 `name`

未知的配置键会抛错 `e.sqlx.var.invalid`，写错了能立刻发现。

## 条件值语法（where 的核心）

`${@vars=where}` 把上下文（一个 JSON）翻译成 WHERE 条件。
入口是 `SqlCriteria.toCriNode`：**值的类型与写法决定条件形态，
键的写法决定组合与取反**。下面按「整体结构 → 键 → 值」三层展开，
所有例子的渲染结果都有 `SqlCriteriaNodeTest` 的断言保证。

### 整体结构

- 顶层是**对象**：每个键一条条件，条件之间 **AND** 连接
- 顶层是**数组**：每个元素一条条件（元素可以是对象或单个值），
  元素之间 **OR** 连接
- **`$and` / `$or` 组合键**（也认 `#and` / `#or`，键名后可带 `:任意后缀`）：
  值再按上述规则递归解析，`$or` 与前面的条件 OR 连接
- ⚠️ 当前实现里 `$and` 的值若是数组，数组元素之间是 **OR** 连接：
  `{a:'A',$and:[{x:100},{y:99}]}` → `a='A' AND (x=100 OR y=99)`
  （见 `SqlCriteriaNodeTest#test_and_or_list`）。
  需要严格的组内 AND 时，把条件平铺到顶层即可
- **忽略的键**：`__` 开头的键，以及 `$io` / `$sys` / `$vars`
  （这些是上下文保留键，不会当成字段）

### 键的写法

- 默认键名就是字段名；可用 `fpref` 加前缀、`mapping` 改名（见上文配置项）
- **`!` 前缀：整条条件取反**
  - `{"!age":[18,]}` → `NOT age>=18`
  - `{"!a":"AA"}` → `NOT a='AA'`
  - 与值自身的取反语法（见下）可叠加，双重否定会互相抵消

### 值的写法

**数字与布尔** → 相等比较：

```java
{age:25}    → age=25
{ok:true}   → ok=1        // 布尔按 1/0 处理
```

**null 与「非空」**：

```java
{phone:null}       → phone IS NULL
{phone:"<NULL>"}   → phone IS NULL      // 字符串 "<NULL>" 等价
{phone:""}        → NOT phone IS NULL  // 空串表示「字段非空」
```

**字符串**，按前缀/内容依次识别（`SqlCriteria.strToExp`）：

```java
{name:"!Be"}      → name!='Be'         // ! 前缀：不等于
{name:"!"}         → NOT name IS NULL   // 单个 !：非空
{name:"^be"}      → name REGEXP 'be'   // ^ 前缀：正则
{name:"!^be"}     → NOT name REGEXP 'be'
{name:"*Be?"}     → name LIKE '%Be_'   // 含 * / ?：模糊，*→%，?→_
{name:"Str[2025,2026]"} → (name>='2025' AND name<='2026')  // 范围串
{name:"Be"}       → name='Be'          // 其余：精确匹配
```

**数组 / 集合** → IN 枚举：

```java
{id:[1,2,3]}      → id IN (1,2,3)
```

**Map** 值 → 两种解释（依次尝试，`SqlCriteria.mapToExp`）：

- **操作符键**：`$eq` / `$ne` / `$gt` / `$gte` / `$lt` / `$lte` / `$in` / `$nin`
  （`%` 前缀等价，如 `%eq`）。多个操作符之间 AND 连接；
  `$in` / `$nin` 的值是数组：

```java
{ct:{$gte:'2025',$lte:'2026'}}  → (ct>='2025' AND ct<='2026')
{ct:{$ne:'2025'}}               → NOT ct='2025'
{id:{$in:[1,2]}}                → id IN (1,2)
```

- **范围对象**（nutz `Ranges` 的 RangeInfo 风格，适合前端查询表单
  直接输出的结构）：

```java
{ct:{hasMinValue:true,  minValue:"2025",  minValueIncluded:true,
     hasMaxValue:true,  maxValue:"2026",  maxValueIncluded:true}}
→ (ct>='2025' AND ct<='2026')
```

六个键：`hasMinValue` / `minValue` / `minValueIncluded` /
`hasMaxValue` / `maxValue` / `maxValueIncluded`；
`minValueIncluded:false` 时用 `>`（开区间），右端同理。
见 `SqlCriteriaNodeTest#test_str_info_range`。

### 范围串语法

形如 `[1,12]`、`"(1,12]"`、`"[1,)"`、`"(,20]"` 的字符串：

- `[` / `]` 含端点，`(` / `)` 不含；某一端为空表示无上界/下界
- 单值 `"[20251223]"` 是**精确匹配**：`ct='20251223'`
- 按内容自动识别类型：
  - 纯数字：≤9 位按 int、≥9 位按 long、含小数点按 float
  - `Str[...]` / `Str(...)`：字符串比较，常用于日期串
  - `Date[...]` / `Ms[...]`：日期范围，端点支持时间宏
    （由 `Wregion.extend_rg_macro` 展开，宏写法参见 `Wtime`）
- 两端都有值时输出带括号：`(age>=1 AND age<=12)`；只有一端则只输出一端

### 渲染与参数

- **文本模式**（调试/`@view`）：字符串值加单引号并转义后直接拼入
- **预编译模式**：值输出为 `?`，按出现顺序收集为参数列表，
  交给 `PreparedStatement`，不存在注入风险
- `alias_or` 声明的键，会对每个别名各生成一条同值条件再 OR 连接，
  值的写法完全遵循本节规则

### alias_or：跨字段模糊搜索

`alias_or=kw:name|nick|email` 声明：上下文里的 `kw` 键，要变成
`(name LIKE ? OR nick LIKE ? OR email LIKE ?)` 的 OR 条件组：

```sql
-- @name = pet.search2
SELECT * FROM t_pet WHERE 1=1
${@vars=where;prefix=AND;alias_or=kw:name|nick}
```

传入 `{kw:"Be"}` 即渲染为 `AND (name LIKE '%Be%' OR nick LIKE '%Be%')`。
多组之间用逗号分隔：`alias_or=kw:name|nick,tag:a|b`。

## 控制流

sqlx 模板复用 `WnTmplX` 的解析器，因此也支持条件与循环
（语法定义在 `WnTmplToken`）：

```sql
-- @name = pet.select2
SELECT * FROM t_pet WHERE 1=1
${#if city}
  AND city='${city}'
${#end}
```

- `${#if 条件}` … `${#else-if 条件}` … `${#else}` … `${#end}`：
  条件用 `WnMatch` 语法（JSON5 对象，如 `${#if age>18}` 写作 `${#if {age:18..100}}`，
  或以 `not` 开头取反）
- `${#loop 变量,下标=起始 : 列表键}` … `${#end}`：遍历上下文中的列表，
  `${#break}` / `${#continue}` 控制循环

控制流在 SQL 模板里用得不多——绝大多数动态场景 `@vars=where` 就够了，
它主要服务于更一般的文本模板。列在这里是让你读到别人的模板时认得出来。

## 执行与批量：参数如何收集

模板渲染为 `?` 占位后，参数按出现顺序从上下文收集为参数列表，
交给 `PreparedStatement` 执行——不存在注入风险。上下文有两种形态，
对应 `@exec` 的两种执行方式：

- **MAP 模式（默认）**：上下文是单个对象，渲染一次、执行一次
- **LIST 模式**：`sqlx @vars -as list` 把上下文切为列表（输入本身是数组时
  自动进入），`@exec` 会对列表里**每条记录各渲染一次模板并执行一次**

列表模式下加 `-batch` 开启**批量模式**（`sqlx @exec <名称> -batch`）：

- 用第一条记录渲染出 SQL 与参数结构，之后每条记录按同一结构各收集
  一组参数，一次性 `addBatch` / `executeBatch` 发给数据库
- 代价是要求列表里每条记录的字段结构一致（同一份模板渲染出的
  占位符个数与顺序相同），适合「批量插入/更新同构数据」

执行时 SQL 与参数会写入日志（info 级），排查「执行的到底是哪条 SQL」
非常方便——单条模式打印第一行参数，批量模式打印 `Params xN:` 及前 3 组：

```text
INSERT INTO t_sys_cron_log (st,ct,lm,c_id,m_id,type,duration,start_at,done_at,batch,params,result,id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
Params: [1, "2026-09-23 00:20:50.510", "2026-09-23 00:20:50.510", "demo", "demo", "GUI", 231, "2026-09-23 00:20:50", "2026-09-23 00:20:50", null, null, "{\"N\":2}", "mudcxbkesbt37adxp8"]
```

第一行是带 `?` 的预编译 SQL，第二行就是按顺序对应的参数值。

## 调试方法

模板写错了，最快的定位方式是看渲染结果：

```bash
# 看某个实体渲染出来的最终 SQL（文本模式，值直接拼进去）
sqlx @view pet.select -vars '{name:"Be"}'

# 只看 @vars 部分解析出的参数
sqlx @vars '{name:"Be", age:[1,12]}'

# 模板文件改动后清缓存，避免读到旧模板
sqlx @cache -flush
```

文本宏 `%date:now` / `%ms:now` 等在 jsbin 文档中有完整说明，
sqlx 与 jsbin 共用同一套 `WnTmplX` 机制。

# 延伸阅读

- `sqlx` 命令的完整用法（子命令、事务、批量执行等）：
  `src/com/site0/walnut/ext/data/sqlx/man/zh_cn/cmd_sqlx.md`
- 模板渲染引擎的通用机制（`${...}` 解析、文本宏）：
  ../40.commands/20-cmd-jsc.md 与 ../10-write-jsbin.md 中关于 `WnTmplX` 的部分
- 条件语法实现：src/com/site0/walnut/ext/data/sqlx/ast/SqlCriteria.java
  （入口 `toCriNode`，值/键的解析规则都在里面）
