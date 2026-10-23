# 命令简介

`bizhook` 读取一个钩子配置文件，对输入的对象（WnObj 或 JSON）按规则匹配并执行命令模板。

配置文件描述一组钩子：每个钩子用 `match` 条件（正则匹配对象字段）挑选对象，命中后执行 `commands` 命令模板（模板里可用 `${obj.xxx}` 引用对象字段）。

# 用法

```bash
bizhook 配置文件 @处理器 [对象...] [-batch] [-tail] [-limit N]
                        # 对象    # 对象 id/路径、JSON 字符串；不给则从管道读
  -batch    # 批量模式：执行所有命中的钩子；默认只执行第一个命中的
  -tail     # 从钩子列表尾部开始查找
  -limit    # 限定最多命中多少个钩子，批量模式默认不限，非批量默认 1
```

支持的处理器：

```bash
@test    # 只打印每个对象命中的钩子，不执行
@run     # 执行命中的钩子命令
```

# 示例

```bash
# 先测试哪些钩子会被命中
bizhook ~/myhook.json @test id:4t8a..23a0

# 根据指定对象的元数据执行钩子
bizhook ~/myhook.json @run id:4t8a..23a0

# 多个对象分别执行
bizhook ~/myhook.json @run a.txt b.txt c.txt

# 对象内容从管道读入
cat demo.json | bizhook ~/myhook.json @run

# 批量模式：执行所有命中的钩子，最多 3 个
bizhook ~/myhook.json @run -batch -limit 3 demo.json
```
