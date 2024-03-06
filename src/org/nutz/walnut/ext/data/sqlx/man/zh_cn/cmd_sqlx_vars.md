# 过滤器简介

`@vars` 为当前上下文指定变量集合

这个变量集合中的变量将与模板中的占位符 `${@vars}` 结合输出特定的sql条件或者插入字段。
嗯，你也可以直接在模板中通过通用占位符 `${varName}` 使用这些变量。

对于列表或者是判断条件，可以通过模板中的 `${#loop}` 以及  `${#if}` 来控制模板渲染结果
详细语法，请参加： `man tmpl`

# 模板占位符`@vars`详解

在一个 SQL 文件的模板中，除了了标准的占位符，还有一种特殊的`动态变量占位符`
它专门根据上下文变量，来设置适合:

- `WHERE` 子句的条件: `${@vars=where}`
- `UPDATE SET` 子句的字段:  `${@vars=update}`
- `INSERT INTO` 子句的字段名称: `${@vars=insert.columns}`
- `VALUES` 子句的值列表: `${@vars=insert.values}`

这样说太抽象，下面我们来举个例子：

```js
// 我们假设上下文变量为：
{name:"xiaobai", race:"cat", color:"white", age:6}
```


## `@vars=insert.columns|values`  适用于 `INSERT INTO` 子句

```sql
-- INPUT:
INSERT INTO t_pets (${@vars=insert.columns}) VALUES (${@vars=insert.values})

-- Output:
INSERT INTO t_pets (name,race,color,age) VALUES (?,?,?,?)

-- Params:
["name", "race", "color", "age"]
```

## `@vars=update`  适用于 `UPDATE SET` 子句

```sql
-- INPUT:
UPDATE t_pets SET ${@vars=update; omit=name,race;} WHERE name = '${name}'

-- Output:
UPDATE t_pets SET color=?, age=? WHERE name = 'xiaobai'

-- Params:
["color", "age"]
```

## `@vars=where`  适用于 `WHERE` 子句

> 占位符变量: ` ${@vars=where; omit=race;}` 将输出:

```sql
-- INPUT:
SELECT * FROM t_pets WHERE ${@vars=where; omit=race;}

-- Output:
SELECT * FROM t_pets WHERE name=? AND color=? AND age=?

-- Params:
["name", "color", "age"]
```

在这个模式下，逻辑稍微有点复杂。因为我们希望上下文变量可以描述更加复杂的查询条件。
我们需要支持：

- 精确匹配
- 范围匹配
- 枚举匹配
- 模糊匹配

因此我们支持上下文变量，有下面这些可能：

- `age:"[3,100)"` : _范围匹配_ : `age>=3 AND age<100`
- `name:"^ABC"` : _正则匹配_ : `name REGEXP '^ABC'`
- `name:"*A?C"` : _模糊匹配_ : `name LIKE '%A_C'`
- `name:null` : _空匹配_ : `name IS NULL`
- `city:""` : _存在匹配_ : `city IS NOT NULL`
- `color:['red','blue']` : _枚举匹配_ : `color IN ('red','blue')`
- 同时支持在所有的键名开始处 用 `!` 表示取反，即：
   - `"!age":"[3,100)"` : _取反匹配_ : `NOT (age>=3 AND age<100)`

由于我们需要采用 SQL 语句的 `?` 参数，以便在查找的时候规避sql注入。
因此这个战斧变量暴露出来的动态参数。与上面的格式不同，应该是不一样的。
为此我们不得不构建一个where语句的抽象语法树，这个语法树。从逻辑上来说，应该是下面这样的：

```bash
# AST of WHERE
|-- Node1         # 语法树的根节点
     |
   <AND>
     |
    Node2 --> Node2.1
     |          |
   <AND>       <OR>
     |          |
    Node3     Node2.2
                |
               <OR>
                |
              Node2.3

```

> 说他是一棵语法树，它其实更像一个多层嵌套的列表。

语法树的节点。有两种类型: `Expression` 以及 `Group`。
顾名思义。只有 `Group` 才会有子节点。
每个节点都有下面三个属性：

- `not` : 表示当前节点是否是取反。
- `nextNode` : 指向下一个子节点。
- `nextJoin` : 与下一个字节点连接的方式，只能是 `AND|OR` 

因此只要我们得到任意一个节点实例。我们就能从从这个节点直接渲染出一个sql语句。

# 用法

```bash
sqlx @vars 
  [{Vars}...]        # 参数为多个变量集合，如果未指定，则从标准输入读取
  [-omit {..}]       # 在输入的变量集中移除指定的变量
  [-pick {..}]       # 在输入的变量集中仅挑选指定的变量
```

# 示例

```bash
# 增加一个变量集合
sqlx @vars 'name:"xiaobai",age:12'

# 添加多个变量集合
sqlx @vars 'name:"John Doe", age:30, city:"New York"' 'gender:"male"' 

# 添加变量集合并移除一个变量
cat ~/myvars.json | sqlx @vars -omit 'age'

# 添加变量集合并选择特定的变量
sqlx @vars 'name:"Albert Einstein", field:"Physics"' -pick 'name'

# 从一个文件中读取变量并选择某些变量
cat variables.txt | sqlx @vars -select 'username, password'
```
