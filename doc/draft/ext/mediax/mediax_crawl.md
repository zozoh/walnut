---
title  : MediaX爬取操作
author : zozoh
tags:
- 扩展
- mediax
---

# 普通爬取

```
# 指定 URI
mediax crawl http://icp.chinaz.com/provinces

# 快捷
mediax crawl http://icp.chinaz.com 最新备案
```

# 带条件爬取

```
# 指定 URI
mediax crawl http://icp.chinaz.com/provinces?prov=京&domain=10

# 快捷
mediax crawl http://icp.chinaz.com 最新备案/京
```

# 指定起始翻页

```
# 指定 URI
mediax crawl http://icp.chinaz.com/provinces?prov=京&domain=10&page=3

# 快捷
mediax crawl http://icp.chinaz.com 最新备案/京/3
```