# 过滤器简介

`@dvcheck` 用数据版本号做乐观锁检查。

调用方提供：

1. `before`：期望的数据库版本
2. `after`：校验通过后要写入的版本
3. 一个检查 SQL：通常是 `UPDATE ... SET ver=after WHERE id=? AND ver=before`
   —— 更新 1 行表示通过，0 行表示版本已变

# 用法

```bash
sqlx @dvcheck
  [sqlName]           # 执行检查的 SQL 模板
  [versionKey]        # 版本字段名
  [$before:$after]    # 期望版本:成功后版本
```

- 检查 SQL 里的变量来自当前 `@vars`，可用 `${@vars=...}` 传参
- 检查通过后继续执行后续过滤器；失败会报错

# 示例

```bash
sqlx @vars '{id:"xxx", title:"world"}' \
  @dvcheck pet.check ver v0:v2 \
  @exec pet.update
```
