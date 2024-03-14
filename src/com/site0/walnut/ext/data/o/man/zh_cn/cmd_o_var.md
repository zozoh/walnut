# 过滤器简介

`@var` 更新上下文中的对象

# 用法

```bash
o @var
  [Name]           # 变量名
  [Value]          # 变量值，通过 $xxx 可以引用环境变量
  [-dft xxx]       # 指定变量默认值，否则默认值为 null
  [-autojava]      # 因为环境变量或者输入的值都是字符串
                   # 开启这个选项，可以自动将变量值变成Java的数据类型
                   # 譬如 "8" 变成整型，"true" 变成布尔
  [-view]          # View 模式将会输出指定的变量
                   # 在这个模式下，所有的参数都是变量名
```

# 示例

```bash
# 设置一个环境变量
o id:xxx @var a $AGE @update 'age:"=a"' -explain
```

