# 过滤器简介

`@recall` 重新执行一个请求文件。

# 用法

```bash
httpapi @recall API路径 请求对象路径
       # API路径        # API 的路径
       # 请求对象路径    # 请求对象路径
```

# 示例

```bash
# 重新执行指定请求对象
httpapi @recall /get/hello ~/.regapi/tmp/xxxx
```
