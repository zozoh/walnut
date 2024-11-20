# 过滤器简介

`@json` 将上下文的结果输出为 json

# 用法

```bash
sqlx @json
  [-cqn]              # JSON 格式化方式
  [-as auto|raw]      # 处理 result 的方式
                      # - raw  : 【默认】什么都不处理直接原生输出
                      # - auto : 自动处理，如果 result 是数组或者集合
                      #          且只有一个元素，则将自动输出第一个元素
                      #          如果是 SqlExecResult ，则隐藏 batchResult 的细节
                      # - obj  : 如果 result 是数组或者集合，强制输出第一个元素
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
