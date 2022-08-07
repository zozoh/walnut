# 过滤器简介

`@mime` 给出对象 MIME 类型列表。
本过滤器将会禁止全局输出

# 用法

```bash
o @mime
  [jpg png ..]  # 可以指定多个文件类型
                # 如果没有指定，将展示全部 mime
  #--------------------------------------
  #  - list  : 以列表的形式展示 mime
  #  - map   : 以 Map 的形式展示 mime 
  #  - value : 一行一个输出查询到的 MIME
  #  - line  : 一行一个输出查询到的 MIME，显示类似 jpg=image/jpeg 【默认】
  [-as value]
  #--------------------------------------
  [-cqn]     # 指定的格式化方式
```

# 示例

```bash
o @get ID1 ID2 ID3 @ajax -cqn
```

