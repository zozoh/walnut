# 过滤器简介

`@output` 设定响应的输出目标
 

# 用法

```bash
httpc {URL} @output 
# 指定目标输出路径，这里有两种情况
# 1. 如果目标已经存在，且为目录；或者路径是以 '/' 结尾
# 2. 否则必然指定了一个文件的路径
[~/path/to.txt]      
# 开启这个选项，即使指定了下载文件路径，如果响应里带有
# "Content-Disposition"，也会用其来作为文件名
# 否则会优先使用传入的路径作为文件名
[-autoname]                  
```

# 示例

```bash
# 下载到文件
httpc http://mysite.com/path/to/MyPet.mp4 @output ~/tmp/
> MyPet.mp4

# 下载到文件, 如果响应的 Content-Disposition : "attachment; filename="MyPet.mp4""
httpc http://mysite.com/abc.txt @output ~/tmp/
> MyPet.mp4

# 下载到文件, 如果响应的 Content-Disposition : "attachment; filename="MyPet.mp4""
httpc http://mysite.com/path/to/data001 @output ~/tmp/ 
> MyPet.mp4

# 下载到文件, 如果响应的 Content-Disposition : "attachment; filename="MyPet.mp4""
# 会优先覆盖
httpc http://mysite.com/path/to/MyPet.mp4 @output ~/tmp/abc.jpg -autoname
> MyPet.mp4
```
