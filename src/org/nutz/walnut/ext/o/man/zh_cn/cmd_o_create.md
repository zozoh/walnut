# 过滤器简介

`@create` 创建一个或者多个对象，并将它们加入上下文

# 用法

```bash
o @create 
  [-p /path/to/parent]    # 指定一个父目录，如果是文件，采用其所在目录
  [-race FILE]            # 指定创建的是文件还是目录，默认为 FILE
                          # 如果输入的是 Meta，本参数将作为默认值
  [nameOrMeta ...]        # 多个文件名或者元数据
```




# 示例

```bash
# 在当前目录下创建一个 abc.txt
o @create abc.txt

# 在当前目录下创建一个指定元数据的文件对象
o @create '{nm:"xiaobai", age:12}'

# 从标准输入创建一组文件
# 这个 JSON 文件可以是一个数组，或者单个一个对象元数据
# 如果数组元素是个字符串，则表示文件名
cat abc.json | o @create

# 直接创建多个对象
o @create a.txt b.txt

# 在某个指定目录下创建多个对象
o @create -p ~/my/dir x.txt y.txt z.txt
```

