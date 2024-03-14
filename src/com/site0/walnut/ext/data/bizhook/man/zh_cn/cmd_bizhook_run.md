命令简介
======= 

`bizhook test` 用来真正运行一个 `bizhook`

用法
=======

请参看 `man bizhook`

示例
=======

```bash
# 根据指定对象的元数据，执行一个 hook
bizhook ~/myhook.json run id:4t8a..23a0

# 根据多个对象元数据分别执行 hook
bizhook ~/myhook.json run a.txt b.txt c.txt

# 根据对象内容执行 hook
cat demo.json | bizhook ~/myhook.json run
```
