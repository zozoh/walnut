# 过滤器简介

`@msetnx` 批量版 `@setnx`（对应 Redis `MSETNX`）：**只有所有键都不存在**才全部写入。

返回 `1`（全部写入成功）或 `0`（有任一键已存在，一个都不写）。

# 用法

```bash
redis @msetnx -map '{键1:值1, 键2:值2, ...}'
```

# 示例

```bash
# 首次初始化，重复执行不会覆盖
redis @msetnx -map '{cfg:version:1, cfg:author:"zozoh"}'
```
