# 过滤器简介

`@can` 根据权限矩阵检查给定权限，输出是否具备这些行为的权限。

# 用法

```bash
pvg [权限矩阵路径] @can 角色 [行为...] [-or] [-ajax] [-cqn]
       # 权限矩阵路径    # 不给则从管道读取矩阵 JSON
       # 角色            # 角色名称，多个用半角逗号分隔
       # 行为            # 要检测的行为，可多个
  -or      # 只要求具备其中至少一个行为的权限；默认要求全部具备
  -ajax    # 结果用 Ajax 格式包裹（{ok:true,data:{行为:bool,...}}）
  -cqn     # JSON 输出的格式化开关
```

# 示例

```bash
# 是否具备某个行为的权限
pvg ~/my-pvg.json @can Role1 Action-B
# true

# 是否全部具备某几个行为的权限
pvg ~/my-pvg.json @can Role1 Action-B Action-C
# false

# 是否具备某几个行为中至少一个的权限
pvg ~/my-pvg.json @can -or Role1 Action-B Action-C
# true

# Ajax 格式输出
pvg ~/my-pvg.json @can Role1 Action-B Action-C -ajax -cqn
# {"ok":true,"data":{"Action-B":true,"Action-C":true}}
```
