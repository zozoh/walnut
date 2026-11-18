# 过滤器简介

`@cache` 查看或清理 `sqlx` 的 SQL 模板缓存。

模板按目录加载并缓存在当前服务进程内；改了 `~/.sqlx/*.sql` 后如果仍读到旧内容，
用 `-clear` 清理。

# 用法

```bash
sqlx @cache
  [-get xxx]    # 只查看匹配的模板；支持通配符，如 pet*
  [-clear]      # 清空模板缓存
```

- 不带 `-get` / `-clear`：输出全部已缓存模板
- `-clear` 只清当前命令实例的模板缓存，不修改磁盘文件

# 示例

```bash
# 看某个模板是否已加载
sqlx @cache -get pet.select

# 看 pet 下所有实体
sqlx @cache -get 'pet*'

# 清缓存
sqlx @cache -clear
```
