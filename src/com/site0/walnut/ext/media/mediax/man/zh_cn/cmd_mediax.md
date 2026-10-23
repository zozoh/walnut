# 命令简介

`mediax` 提供第三方媒体平台信息的爬取和发送功能。

# 用法

```bash
mediax [账号] @处理器 [参数...]
  # 账号    # ~/.mediax 下的账号目录名，可省略
```

支持的处理器：

```bash
@post       # 投递一条图文素材到指定目标（暂未实现）
@crawl      # 爬取一批内容索引，返回的总是一个 JSON 数组
@download   # 下载指定资源（暂未实现）
@target     # 显示每个媒体平台的快捷目标
@explain    # 将快捷目标解析为真实的 URL
```

# 示例

```bash
# 匿名爬取
mediax @crawl http://host/path/to/resource

# 指定账号爬取
mediax zozoh @crawl http://host/path/to/resource
```
