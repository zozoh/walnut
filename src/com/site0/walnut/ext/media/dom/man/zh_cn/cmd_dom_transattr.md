# 过滤器简介

`@transattr` 将当前 dom 指定节点属性，映射到新值

# 用法

```bash
dom @transattr
  [selector1, selector2]    # 元素选择器，譬如 [wn-obj-id]
  [-attr id]                # 要处理的属性名称 
  [-mapping '{...}']        # 映射集合，键为原始值，值为映射的值
                            # 如果不是 {} 形式的 JSON 则认为是一个路径
                            # 如果未声明则尝试从标准输入读取
```