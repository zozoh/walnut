---
title: 编写 SQL 模板（sqlx）
module: 101.dev
keywords: [sqlx, SQL 模板, @vars, 动态 SQL, where, SqlEntry, 条件值]
source:
  - ext/data/sqlx/loader/SqlEntry.java
  - ext/data/sqlx/tmpl/WnSqlTmpl.java
  - ext/data/sqlx/tmpl/WnSqlElementMaker.java
  - ext/data/sqlx/tmpl/vars/SqlVarsElement.java
  - ext/data/sqlx/ast/SqlCriteriaNode.java
related:
  - ../../src/com/site0/walnut/ext/data/sqlx/man/zh_cn/cmd_sqlx.md
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

把上下文（或 `scope` 指定的子树）渲染为条件表达式，见下一节的条件值语法：

```sql
SELECT * FROM t_pet WHERE 1=1 ${@vars=where;prefix=AND}
```

- 上下文为空（或所有字段被 pick/omit/ignoreNil 过滤完）时输出空字符串
- `prefix` 在条件非空时输出，前后自动带空格（`prefix=AND` → ` AND `），
  所以模板上写 `WHERE 1=1 ${@vars=where;prefix=AND}` 即可，不用操心前面留不留空格
- 还可以用 `dft` 指定上下文为空时的兜底 SQL

### `order`：动态排序

把上下文的键值对渲染为 `key ASC, key DESC` 列表：

```sql
SELECT * FROM t_pet ${@vars=order}
```

- 值为 `"desc"`（不区分大小写）或负数 → `DESC`，其余 → `ASC`
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

`${@vars=where}` 渲染上下文时，**值的不同写法决定条件的形态**。
以下语义均有 `SqlCriteriaNodeTest` 的断言保证，写法可以直接抄：

```java
// 区间：方括号包含，圆括号不包含，空端表示无限
{age:[1,12]}     → (age>=1 AND age<=12)
{age:"(1,12]"}   → (age>1 AND age<=12)
{age:"[1,)"}     → age>=1

// Str 前缀写法，常用于日期等字符串比较
{ct:"Str[2025,2026]"}  → (ct>='2025' AND ct<='2026')

// Map 写法，操作符键 $gte/$lte/$gt/$lt/$eq/$ne
{ct:{$gte:'2025', $lte:'2026'}}  → (ct>='2025' AND ct<='2026')

// 模糊匹配：* 任意多字符，? 单字符
{name:"*Be?"}    → name LIKE '%Be_'

// 正则：^ 开头
{name:"^be"}     → name REGEXP 'be'

// 数组：IN
{id:[1,2,3]}     → id IN (1,2,3)

// 逻辑值与空
{ok:true}        → ok=1
{phone:null}     → phone IS NULL
{phone:''}       → NOT phone IS NULL      // 空串表示「非空」

// 键前缀 ! 表示 NOT
{"!age":[18,]}   → NOT age>=18
```

组合规则：

- 顶层 Map 的多个键之间是 **AND**
- 顶层数组的多个元素之间是 **OR**（每个元素可以是 Map 或条件串）
- `$and:[...]` 组内的元素之间目前是 **OR** 连接
  （`{a:'A',$and:[{x:100},{y:99}]}` → `a='A' AND (x=100 OR y=99)`，
  这是当前实现的行为，见 `SqlCriteriaNodeTest#test_and_or_list`；
  如果你需要严格的组内 AND，把条件平铺到顶层即可）
- 所有语法都可以套 `NOT (...)` 整体取反（`setNot`）

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
- 条件语法实现：src/com/site0/walnut/ext/data/sqlx/ast/SqlCriteriaNode.java
