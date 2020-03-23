命令简介
======= 

`bizhook test` 用来测试一个 `bizhook`，它不真的运行钩子，只是打印一下匹配结果。

用法
=======

请参看 `man bizhook`

示例
=======

```bash
demo:> bizhook ~/myhook.json test {x:77} {y:99}
Bean[0]
 > Found 1 hooks
 - [0] M(1): echo `date` > ~/.domain/bizhook/abc_result
Bean[1]
 > Found 0 hooks
```
