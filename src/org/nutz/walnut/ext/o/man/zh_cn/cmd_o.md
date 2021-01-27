# 命令简介

`o` 用来处理系统对象相关的操作，包括增删改查以及其他的高级操作
    
# 用法

```
o [options] [[@filter filter-args...]...]
```

它支持的`options`有

```bash
[PATH1 PATH2 ...]  # 多个对象
[-cqn]             # JSON 的格式化方式
[-l]               # 输出时强制列表，否则只有一个列表项时输出对象
# 对象的哪个键用来存储 children，默认为 children
# 对于 @json/ajax/tree 这样的子命令，会用到这个设置
[-subkey children]
# 如何处理不存在的路径
#  null   : 标记 null
#  ignore : 忽略
# 默认的，会抛错
[-noexists null|ignore]
```

# 过滤器列表

```bash
@tree       # 将上下文对象归纳成一棵树
@ancestors  # 查询上下文中的对象的所有祖先对象
@create     # 创建一个或者多个对象
@query      # 查询出一组对象
@update     # 更新对象的字段
@refer      # 读取上下文对象关联的对象详情
@get        # 根据 ID 获取一个或几个对象
@fetch      # 根据路径获取一个或几个对象
@remove     # 删除对象
@filter     # 过滤上下文列表对象
@children   # 读取其子对象
@group      # 聚集对象
@push       # 压入对象数组字段
@pop        # 弹出对象数组字段
@clear      # 清除上下文
@json       # 将上下文输出为 JSON
@ajax       # 将上下输出为 AJAX
@tmpl       # 将上下文输出为行模板
@tab        # 将上下文输出为表格
@quiet      # 禁止上下文输出
@value      # 将上下文中的对象依次取值，并输出
```
