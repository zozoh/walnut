`xo update` 修改第三方平台对象存储中对象的元数据

# 用法

```bash
xo update \
  [key]           # 【必】对象的键名
  [delta]         # 要更新的元数据（可选，如果不提供则从标准输入读取）
```
# 示例

```bash
# 更新对象元数据
xo s3:file @update hello.txt '{"author":"updated","version":2}'

# 从标准输入读取元数据更新
cat meta.json | xo s3:file @update hello.txt
```