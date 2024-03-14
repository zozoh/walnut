# 命令简介 

`dsync @restore` 将归档包展开到缓存

# 用法

```bash
dsync @restore
  [-q -quiet]    # 静默输出
  [-f -force]    # 强制覆盖，否则对于元数据，则检查是否相同
                 # 对于数据，则检查一下 SHA1
```
# 示例

```bash
# 输出对于的归档包名称
demo@~$ dsync @tree 575b..e964a @restore
 =-d> D:~/15..d6/.data/albums;=BEAN((2e..80));=META((dc..99))
 =-f> F:~/15..d6/a.jpg;=BEAN((a3..65));=META((6d..49));=SHA1((c9..f6));=LEN(4057)
 =-f> F:~/15..;=BEAN((4a..90));=META((81..4c));=SHA1((f9..ce));=LEN(213)
 Done for restore null in Total: 44832ms
item_count: 0
dir_count: 0
file_count: 0
size_count: 0Bytes
```