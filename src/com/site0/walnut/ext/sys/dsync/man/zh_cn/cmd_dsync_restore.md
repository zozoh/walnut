# 过滤器简介

`@restore` 将归档包展开到缓存。

# 用法

```bash
dsync @restore [-q] [-f]
  -q       # 静默输出
  -f       # 强制覆盖；否则对于元数据检查是否相同，对于数据检查 SHA1
```

# 示例

```bash
# 加载指定指纹的索引树并展开归档包
dsync @tree 575b..e964a @restore
# =-d> D:~/15..d6/.data/albums;=BEAN((2e..80));=META((dc..99))
# =-f> F:~/15..d6/a.jpg;=BEAN((a3..65));=META((6d..49));=SHA1((c9..f6));=LEN(4057)
# ...
# Done for restore null in Total: 44832ms
```
