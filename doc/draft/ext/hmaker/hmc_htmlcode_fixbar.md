---
title:控件:代码片段:固定式停靠条
author:zozoh
tags:
- 扩展
- hmaker
---

# 停靠条概述

这个一个 HTML 代码片段, 遵守下面的 DOM 结构

```
<ul>
<li>
<a target="_blank" href="http://weibo.com"><i class="fa fa-weibo"></i></a>
</li>
<li>
<a target="_blank" href="http://weibo.com"><i class="fa fa-weixin"></i></a>
<img class="enter-show" src="image/service_r3.jpg">
</li>
<li a="top">
<a><i class="fa fa-angle-double-up"></i></a>
</li>
</ul>
```

- 在 `<li>` 内，任意子元素，只要有 "enter-show" 就能变成鼠标进入展出

