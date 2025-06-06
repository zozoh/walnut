# 过滤器简介

`@vars` 为当前上下文指定变量集合

这个变量集合中的变量将与模板中的占位符 `${@vars}` 结合输出特定的 sql 条件或者插入字段。
嗯，你也可以直接在模板中通过通用占位符 `${varName}` 使用这些变量。

对于列表或者是判断条件，可以通过模板中的 `${#loop}` 以及 `${#if}` 来控制模板渲染结果
详细语法，请参加： `man tmpl`

# 模板占位符`@vars`详解

在一个 SQL 文件的模板中，除了了标准的占位符，还有一种特殊的`动态变量占位符`
它专门根据上下文变量，来设置适合:

- `WHERE` 子句的条件: `${@vars=where}`
- `UPDATE SET` 子句的字段: `${@vars=update}`
- `INSERT INTO` 子句的字段名称: `${@vars=insert.columns}`
- `VALUES` 子句的值列表: `${@vars=insert.values}`

同时 `${@vars}` 还可以支持更多的描述属性：

- `${@vars=[type]; scope=query}` : 限定变量范围，
  即，选用上下文中的 `query` 变量作为本次渲染的变量集合，
  默认的，会采用整个上下文集合作为渲染变量集合
- `${@vars=[type]; ignoreNil}` : 忽略空值变量
- `${@vars=[type]; omit=a,b,c}` : 忽略指定变量
- `${@vars=[type]; pick=x,y,z}` : 仅选择指定变量

同时你可以组合上面的属性，多个属性用半角分号`;`分隔，
譬如 `${@vars=where; scope=query; pick=name,age}`

下面我们来举个例子：

```js
// 我们假设上下文变量为：
{name:"xiaobai", race:"cat", color:"white", age:6}
```

## `@vars=insert.columns|values` 适用于 `INSERT INTO` 子句

```sql
-- INPUT Template:
INSERT INTO t_pets (${@vars=insert.columns}) VALUES (${@vars=insert.values})

-- Static Output:
INSERT INTO t_pets (name,race,color,age) VALUES ('xiaobai','cat','white',6)

-- Params Output:
INSERT INTO t_pets (name,race,color,age) VALUES (?,?,?,?)

-- Params List:
[name="xiaobai", race="cat", color="white", age=6]
-- 请注意，这个参数表是两个占位符联合在一起，依次拼合出来的
-- 在 JDBC PreparedStament 的时候就特别有用
```

## `@vars=update` 适用于 `UPDATE SET` 子句

```sql
-- INPUT Template:
UPDATE t_pets SET ${@vars=update; omit=name,race;} WHERE name = '${name}'

-- Static Output:
UPDATE t_pets SET color='white', age=6 WHERE name = 'xiaobai'

-- Params Output:
UPDATE t_pets SET color=?, age=? WHERE name = 'xiaobai'

-- Params List:
[race="cat", color="white"]
```

## `@vars=where` 适用于 `WHERE` 子句

```sql
-- INPUT Template:
SELECT * FROM t_pets WHERE ${@vars=where; omit=race;}

-- Static Output:
SELECT * FROM t_pets WHERE name='xiaobai' AND color='cate' AND age=6

-- Params Output:
SELECT * FROM t_pets WHERE name=? AND color=? AND age=?

-- Params List:
[name="xiaobai", color="white", age=6]
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

由于我们需要采用 SQL 语句的 `?` 参数，以便在查找的时候规避 sql 注入。
因此这个占位变量暴露出来的动态参数。随着格式的不同，数量应该是不一样的。
范围和枚举应该会暴露出多个参数。

为此我们不得不构建一个 where 语句的抽象语法树：

```bash
# AST of WHERE
|-- Exp   # 语法树的起始节点
     |
   <AND> # nextJoin
     |   # nextNode
    Group ---> Exp
     |          |
   <AND>       <OR>
     |          |
    Exp        Exp
                |
               <OR>
                |
               Exp

```

> 说他是一棵语法树，它其实更像一个多层嵌套的列表。

语法树的节点。有两种类型: `Expression` 以及 `Group`。
顾名思义。只有 `Group` 才会有子节点。
但无论是哪种节点都有下面三个属性：

- `not` : 表示当前节点是否是取反。
- `nextNode` : 指向下一个子节点。
- `nextJoin` : 与下一个字节点连接的方式，只能是 `AND|OR`

因此只要我们得到任意一个节点实例。我们就能从从这个节点直接渲染出一个 sql 表达式。

## `@vars=param` 适用于 `WHERE` 子句的条件

```sql
-- INPUT Template:
SELECT * FROM t_pets WHERE id=${@vars=param; name=id;}

-- Static Output:
SELECT * FROM t_pets WHERE id='pet0009'

-- Params Output:
SELECT * FROM t_pets WHERE id=?

-- Params List:
[id='pet0009']
```

这种模式下，可能可能经常会用到 SQL 的 LIKE 关键字，它通常是这么使用的

```sql
-- INPUT Template:
SELECT * FROM t_pets WHERE name LIKE ${@vars=param; format=%$[hint]%}

-- Static Output:
SELECT * FROM t_pets WHERE name LIKE '%red%'

-- Params Output:
SELECT * FROM t_pets WHERE name LIKE ?

-- Params List:
[null='red']
```

# 用法

```bash
sqlx @vars
  [{Vars}...]        # 数为多个变量集合，如果未指定，则从标准输入读取
                     # 如果是 =.. 或 =Key 则从全局上下文里获取变量
  [-reset]           # 重置变量集合
  [-view]            # 打印当前变量集合
  [-explain]         # 自动展开要更新元数据的【宏】
                     # 同时，如果上下文返回结果有内容，也尝试用这个返回结果
                     # 做上下文，做一下对象 explain。 这样，前序语句插入的对象
                     # 的 id 等关键字段，可以作为后续操作的输入，这样在插入子表
                     # 这样的场景下，还是非常有用的
  [-merge]           # 新的变量集合会深度与上下文融合
                     # 如果没有开启这个选项，将只是浅层替换
  [-as list|map]     # 参数存放为一个列表还是一个 Map
                     # 通常对于批量插入、更新、删除等操作，需要 list
  [-put xx=xx]       # 从【过滤管线上下文】里获取值，设置到对应的对象里
                     # 【过滤管线上下文】的内容可以由 `@set -save` 来设置
                     # 考虑到批量插入的场景，本参数赋值表达式也需要用当前对象
                     # （无论 list|map）作为上下文来渲染，再解析赋值
                     # 如果要设置多个值，则需要用半角逗号分割，譬如:
                     # <code>"code=pet.${id},name=pet.${name}"</code>
                     # 设置模式有两种:
                     #  - "?code=?pet.id"
                     #    > 对象没值，且上下文有值才设置
                     #  - "?code=!pet.id"
                     #    > 只要对象没值，无论上下文是否有值都设置
                     #  - "!code=?pet.id"
                     #    > 无论对象是否有值，只要上下文有值就设置
                     #  - "!code=!pet.id"
                     #    > 无论对象是否有值，也无论上下文是否有值都设置
                     # 如果 "code=pet.id"
                     # 相当于 "?code=?pet.id"
                     # 如果 "code" 就相当于 "code=code"
                     # 如果 "?code" 就相当于 "?code=?code"
                     # 如果 "!code" 就相当于 "!code=!code"
  [-omit k1,k2..]    # 在输入的变量集中移除指定的变量
  [-pick k1,k2..]    # 在输入的变量集中仅挑选指定的变量
  [-fake {FAKER}]    # 伪造上下文变量，主要用于生成测试数据
                     # {FAKER} 的格式为：
                     # '10:/path/to/fake_bean.json' - 伪造10个对象
                     # '/path/to/fake_bean.json' - 伪造1个对象
  [-lang zh_cn]      # 如果指定了 -fake ，
                     # 那么模拟数据的语言可以通过这个指定
                     # 默认是 zh_cn，还可以支持 en_us
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
