# 命令简介

`o` 用来处理系统对象相关的操作，包括增删改查以及其他的高级操作
    
# 用法

```
o [options] [[@filter filter-args...]...]
```

它支持的`options`有

```bash
[PATH1 PATH2 ...]  # 多个对象
```

# 过滤器列表

```bash
@create     # 创建一个或者多个对象
@query      # 查询出一组对象
@update     # 更新对象的字段
@get        # 根据 ID 获取一个或几个对象
@fetch      # 根据路径获取一个或几个对象
@remove     # 删除对象
@parents    # 查询对象其祖先
@filter     # 过滤上下文列表对象
@children   # 读取其子对象
@group      # 聚集对象
@push       # 压入对象数组字段
@pop        # 弹出对象数组字段
@json       # 将上下文输出为 JSON
@ajax       # 将上下输出为 AJAX
@tmpl       # 将上下文输出为行模板
@tab        # 将上下文输出为表格
@quiet      # 禁止上下文输出
```