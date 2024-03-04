# 过滤器简介

`@vars` 为当前上下文指定变量集合

这个变量集合中的变量将与模板中的占位符 `${@vars}` 结合输出特定的sql条件或者插入字段。
嗯，你也可以直接在模板中通过通用占位符 `${varName}` 使用这些变量。

对于列表或者是判断条件，可以通过模板中的 `${#loop}` 以及  `${#if}` 来控制模板渲染结果
详细语法，请参加： `man tmpl`

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
