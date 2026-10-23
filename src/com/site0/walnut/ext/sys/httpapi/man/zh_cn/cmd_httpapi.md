# 命令简介

`httpapi` 处理 HTTP API 相关的数据操作：把声明好的 API（目录 + 元数据）当作函数来调用。

# 用法

```bash
httpapi @处理器 [参数...]
```

支持的处理器：

```bash
@list      # 列出所有的 API
@invoke    # 执行某个 API
@recall    # 重新执行某个请求文件
```

# 示例

```bash
# 列出所有 API
httpapi @list

# 执行某个 API
httpapi @invoke myapi

# 重新执行某个请求文件
httpapi @recall myapi ~/req/xxx.req
```
