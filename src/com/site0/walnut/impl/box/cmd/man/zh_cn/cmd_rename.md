# 命令简介 

`rename` 修改对象的名称

# 用法

```
rename 
  [/path/to/obj]      # 对象路径
  [{New Name}]        # 对象的新名称
  [-keep]             # 保持模式，即改名不会导致类型也跟着修改
  [-cqn]              # 输出对象的JSON格式化信息
  [-quiet]            # 静默模式，成功后不输出任何信息
```

# 示例

```
# 修改 name
rename ~/abc.txt abc.html

# 根据 ID 修改名称
rename -id 3nc...bvni xyz.txt
```

