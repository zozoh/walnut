# 过滤器简介

`@update` 更新上下文中的对象

# 用法

```bash
o @update
  [{..}]          # 可多个更新字段
  [-explain]      # 对更新元数据进行动态赋值
```

# 示例

```bash
o ~/accounts/index @update 'x:100,y99'
```

