# 过滤器简介

`@exec` 处理 SQL 的缓存


# 用法

```bash
sqlx @cache 
  [-get xxx]   # 指明一个要查看的键，
               # - 如果是字符串，则会直接查询缓存键
  [-clear]     # 本操作是重置缓存
               # 如果不声明这个选项，则表示查看缓存
```

# 示例

```bash
# 获取某个指定的键
sqlx @cache -get pet.select

# 获取以指定的值为前缀的一组SQL模版
sqlx @cache -get pet*
```