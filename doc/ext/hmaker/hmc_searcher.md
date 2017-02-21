---
title:控件:搜索框控件
author:zozoh
tags:
- 扩展
- hmaker
---

# 编辑时 DOM 结构

```
<div class="hm-com hm-com-searcher" ctype="searcher" id="首部关键字">
    <div class="hm-com-W">
        <div class="hmc-searcher">
            <div class="kwd-input"><input placeholder="请输入关键字"></div>
            <div class="kwd-btn"><b>立即搜索</b></div>
        </div>
    </div>
</div>
```

# 输出时 DOM 结构

```
<div class="hm-com hm-com-searcher" id="首部关键字"> 
    <div class="kwd-input"><input placeholder="请输入关键字"></div> 
    <div class="kwd-btn"><b>立即搜索</b></div> 
</div>
```

# getComValue 返回

永远返回字符串表示搜索关键字

