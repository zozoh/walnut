# 过滤器简介

`@test` 测试钩子匹配：对输入的每个对象打印命中的钩子列表，但不真正执行命令。

# 用法

```bash
bizhook 配置文件 @test [对象...] [-batch] [-tail] [-limit N]
  -batch    # 批量模式：列出所有命中的钩子；默认只找第一个
  -tail     # 从钩子列表尾部开始查找
  -limit    # 限定最多命中多少个钩子
```

# 示例

```bash
# 测试两个 JSON 对象分别命中哪些钩子
bizhook ~/myhook.json @test '{x:77}' '{y:99}'
# Bean[0]
#  > Found 1 hooks:
#  - [0] M(1): echo `date` > ~/.domain/bizhook/abc_result
# Bean[1]
#  > Found 0 hooks:

# 测试管道输入的对象
cat demo.json | bizhook ~/myhook.json @test
```
