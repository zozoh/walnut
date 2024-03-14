# 过滤器简介 

`@json` 通过文件内容向上下文增加一组变量
 

# 用法

```bash
@json 
  [~/path1, ~/path2]      # 多个对象路径
  [-mapping {..}]         # 【选】映射方式
```


# 示例

```bash
# 指定内容
mailx @json ~/myvars -mapping 'x:"=pos.x", y:"=pos.y"'
```

