---
title:控件:翻页器控件
author:zozoh
tags:
- 扩展
- hmaker
---

# 编辑时 DOM 结构

```
<div class="hm-com hm-com-dynamic" ctype="dynamic" id="数据列表" hm-actived="yes">
<div class="hm-com-W">
    <div class="hmc-pager"
        pager-type="button" 
        show-first-last="yes" 
        show-brief="yes" 
        free-jump="yes"> 
        <div class="pg_ele pg_btn">
            <a key="first">|&lt;&lt;</a>
            <a key="prev">&lt;</a>
        </div>
        <div class="pg_ele pg_nbs">
            <a>1</a>
            <a>2</a>
            <b>3</b>
            <a>4</a>
            <a>5</a>
            <a>6</a>
        </div>
        <div class="pg_ele pg_btn">
            <a key="next">&gt;</a>
            <a key="last">&gt;&gt;|</a>
        </div>
        <div class="pg_ele pg_brief">第 3 页，共 6页, 300 条记录</div>
    </div>
</div>
</div>
```

# 输出时 DOM 结构

```
<div class="hm-com hm-com-pager hmc-pager" id="翻页条" 
    pager-type="button" 
    show-first-last="yes" 
    show-brief="yes" 
    free-jump="yes"> 
    <div class="pg_ele pg_btn">
        <a key="first">|&lt;&lt;</a>
        <a key="prev">&lt;</a>
    </div>
    <div class="pg_ele pg_nbs">
        <a>1</a>
        <a>2</a>
        <b>3</b>
        <a>4</a>
        <a>5</a>
        <a>6</a>
    </div>
    <div class="pg_ele pg_btn">
        <a key="next">&gt;</a>
        <a key="last">&gt;&gt;|</a>
    </div>
    <div class="pg_ele pg_brief">第 3 页，共 6页, 300 条记录</div>
</div>  
```

# getComValue 返回

永远返回 JSON 对象

```
{
    pn   : 1,     // 第几页
    pgsz : 50,    // 每页多少数据
    skip : 0,     // 需要跳过多少数据，相当于 (pn-1)*pgsz
}
```


