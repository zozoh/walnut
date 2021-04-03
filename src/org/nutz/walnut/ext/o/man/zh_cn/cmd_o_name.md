# 过滤器简介

`@name` 分析对象的文件名，并设置分析结果

# 用法

```bash
o @name [majorNameKey] # 将主文件名存放到哪个键，默认为 name
```

# 示例

```bash
# 获取一个对象，并分析其文件名
o @fetch abc.txt @name @json %NM
{

}
```

