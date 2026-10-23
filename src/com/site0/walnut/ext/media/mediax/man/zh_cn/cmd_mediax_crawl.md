# 过滤器简介

`@crawl` 爬取一批内容索引，返回的总是一个 JSON 数组。

# 用法

```bash
mediax [账号] @crawl URL [快捷路径] [-api 平台] [-limit N] [-last 时间] [-cqn]
       # URL        # 要爬取的资源地址
       # 快捷路径    # 平台的快捷目标路径，用 / 分隔（如 最新备案/京/3）
  -api      # 指定爬取平台（如 toutiao.com）；默认从 URL 推断
  -limit    # 最多爬取多少条
  -last     # 只取该时间之后的内容：毫秒时间戳或日期字符串
  -cqn      # 输出 JSON 的格式化开关
```

# 示例

```bash
# 匿名爬取
mediax @crawl http://host/path/to/resource

# 指定一个用户名爬取
mediax zozoh @crawl http://host/path/to/resource

# 采用快捷路径爬取
mediax @crawl http://icp.chinaz.com 最新备案/京/3

# 指定爬取平台
mediax @crawl http://icp.chinaz.com 最新备案/京/3 -api toutiao.com

# 限制条数和时间
mediax @crawl http://host/list -limit 20 -last '2021-05-01'
```
