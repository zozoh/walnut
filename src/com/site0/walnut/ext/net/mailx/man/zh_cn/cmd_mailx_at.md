# 过滤器简介

`@at` 向上下文增加一个或者多个附件
 

# 用法

```bash
@at 
	[~/path/to/obj ...]  # 多个附件的路径
	[-name xxx.zip]      # 表示将从标准输入获取附件内容
	                     # 附件的名称为本选项指定名称
    [-mime text/plain]   # 指定附件的内容类型，默认为 text/plain
```


# 示例

```bash
# 加载两个附件对象
mailx @at ~/a.jpg ~/b.zip

# 从标准输入加载附件
cat ~/test.zip | mail @at -name tt.zip -mime application/zip
```

