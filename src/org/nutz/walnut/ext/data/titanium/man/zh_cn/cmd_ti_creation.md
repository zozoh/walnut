# 命令简介 

`ti creation` 用来获取某个`DIR`对象下可以被创建子对象的类型列表。

> [视图文档](https://github.com/zozoh/titanium/blob/master/doc/en-us/walnut/creation.md)

-------------------------------------------------------------
# 用法
 
```bash
ti creation 
  [/path/to/obj]      # [A]根据对象自动寻找视图
  [-lang zh-ch]       # [选]输入语言，默认 "zh-cn"
  [-cqn]              # [选]JSON 输出的格式化   
```

-------------------------------------------------------------
# 示例

```
# 根据对象得到可以创建的子对象类型列表
demo:~$ ti creation id:xxx
```