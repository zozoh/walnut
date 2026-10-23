# 过滤器简介

`@run` 对输入的每个对象匹配钩子并执行命中的命令模板。

# 用法

```bash
bizhook 配置文件 @run [对象...] [-batch] [-tail] [-limit N]
  -batch    # 批量模式：执行所有命中的钩子；默认只执行第一个命中的
  -tail     # 从钩子列表尾部开始查找
  -limit    # 限定最多命中多少个钩子，批量模式默认不限，非批量默认 1
```

# 示例

```bash
# 执行单个对象命中的钩子
bizhook ~/myhook.json @run id:4t8a..23a0

# 多个对象分别执行
bizhook ~/myhook.json @run a.txt b.txt c.txt

# 对象内容从管道读入
cat demo.json | bizhook ~/myhook.json @run

# 批量执行所有命中的钩子
bizhook ~/myhook.json @run -batch demo.json
```
