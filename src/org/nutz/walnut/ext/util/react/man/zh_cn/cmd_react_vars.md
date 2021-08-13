# 过滤器简介

`@vars` 管理上下文变量。

# 用法

```bash
o @vars
  ['{a:1,b:3}']     # 设置多个变量
                    # 如果没有，则尝试从标准输入读取
  -clear            # 清空上下文的变量
  -remove a,b,c     # 删除多个上下文变量
```

# 示例

```bash
# 加载两个 JSON 文件配置到上下文
react @config ~/a.json ~/b.json

# 动态决定 JSON 文件的加载路径
react @config ~/a-${update.type}.json
```

