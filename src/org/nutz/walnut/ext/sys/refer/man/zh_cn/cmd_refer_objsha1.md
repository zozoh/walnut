# 命令简介 

`refer objsha1` 将会修改某个系统对象SHA1的引用

# 用法

```bash
refer objsha1 
  [path/to]         # 【必】目标对象的路径
  [$LEN]            # 【必】新内容的长度，Byte
  [SHA1]            # 【必】新的 SHA1
  [2021-12-23]      # 【选】可以指定一个时间戳作为最后修改时间
                    # 如果不指定，则会被系统自动更新为当前时间
```

# 示例
    
```bash
# 为某自定义目标增加引用
demo:> refer objsha1 ~/abc.txt 1560 54af..9a2e
```
