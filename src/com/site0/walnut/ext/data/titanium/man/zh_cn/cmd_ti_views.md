# 命令简介 

`ti views` 用来获取某个对象的视图映射信息。

> [视图文档](https://github.com/zozoh/titanium/blob/master/doc/en-us/walnut/view.md)

-------------------------------------------------------------
# 用法
 
```bash
ti views 
  [/path/to/obj]      # [A]根据对象自动寻找视图
  [-name text-editor] # [A]直接指定视图名称 ，这个更优先
  [-m mapping.json]   # [选]映射文件名，默认 mapping.json
  [-cqn]              # [选]JSON 输出的格式化   
```

- 对象路径与 `-name` 必须要给定一个，否则本命令不知道要输出什么

-------------------------------------------------------------
# 示例

```bash
# 根据对象自动选择一个视图
demo:~$ ti views id:xxx

# 直接输出某个视图
demo:~$ ti views -name "text-editor"
```