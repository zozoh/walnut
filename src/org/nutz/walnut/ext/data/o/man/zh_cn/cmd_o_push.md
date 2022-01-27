# 过滤器简介

`@push` 向上下文中所有对象压入对象数组字段

# 用法

```bash
o @push
  [{..}]          # 多个值
  [-to xxx]       # 【必】向哪个键压入值
  [-uniq]         # 确保数组唯一
  [-raw]          # 如果不声明这个选项，值会被自动解析
                  # 
```

# 示例

```bash
$demo:> o ~/mytest @push abc -to names
{"today":4}
```

