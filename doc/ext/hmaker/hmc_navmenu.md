---
title:控件:菜单条
author:zozoh
tags:
- 扩展
- hmaker
---

# 编辑时 DOM 结构

```
<div class="hm-com skin-navmenu-h-subpop" id="导航菜单"> 
<div class="hm-com-W">
<div class="hmc-navmenu">
    <ul class="mic-top ul-top">
        <li class="li-top" current="yes" open-sub="yes">
            <a target="_blank"><i></i><span>首页</span></a>
            <ul class="ul-sub ul-sub-0">
                <li class="li-sub li-sub-0">
                    <a target="_blank"><i></i><span>产品</span></a>
                </li>
            </ul>
        </li>
        <li class="li-top">
            <a target="_blank"><i></i><span>关于</span></a>
        </li>
    </ul>
</div>
</div>
</div> 
```

# 输出时 DOM 结构

```
<div class="hm-com skin-navmenu-h-subpop" id="导航菜单"> 
<div class="hm-com-W">
<div class="hmc-navmenu">
    <ul class="mic-top ul-top">
        <li class="li-top" current="yes" open-sub="yes">
            <a target="_blank"><i></i><span>首页</span></a>
            <ul class="ul-sub ul-sub-0">
                <li class="li-sub li-sub-0">
                    <a target="_blank"><i></i><span>产品</span></a>
                </li>
            </ul>
        </li>
        <li class="li-top">
            <a target="_blank"><i></i><span>关于</span></a>
        </li>
    </ul>
</div>
</div>
</div> 
```

