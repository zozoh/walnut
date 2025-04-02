# 过滤器简介

`@validate` 用来为检查上下文对象是否符合约束。如果上下文是列表则逐个检查
    
# 用法

```bash
jsonx @validate
  [WnMatch]    # 指定检查的约束
  [-f v.json]  # 约束来自一个文件【更高优先级】
  [-only]      #【选】输入对象必须在集合内(仅MapMatch)
  [-ignoreNil] #【选】空值不检查(仅MapMatch)
```

# 示例

```bash
# 判断一个数字范围
jsonx '{x:100,y:99}' @validate '{x:"[99,101]"}'
> 

# 采用正则表达式判断
jsonx -c '{name:"xiaobai",age:20}' @validate '{name:"^m"}'
> {ok:false,data:[{key:"e.v.invalid",reason:"name"}]}

# 多个条件【或】判断
jsonx -c '{name:"xiaobai",age:20}' @validate '[{name:"^m"},{age:20}]'
> {ok:true}
```