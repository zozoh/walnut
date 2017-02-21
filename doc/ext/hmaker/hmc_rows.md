---
title:控件:水平分栏控件
author:zozoh
tags:
- 扩展
- hmaker
---

# 编辑时 DOM 结构

```
<div class="hm-com hm-layout hm-com-rows" ctype="rows" id="rows_1" hm-actived="yes">
    <div class="hm-com-W">
        <div class="hmc-rows">
            <div class="hm-area" area-id="Area1">
                <div class="hm-area-con">
                    // 这里是其他控件
                </div>
            </div>
            <div class="hm-area" area-id="Area2">
                <div class="hm-area-con">
                    // 这里是其他控件
                </div>
            </div>
        </div>
    </div>
</div>
```

# 输出时 DOM 结构

```
<div class="hm-com hm-layout hm-com-rows" id="rows_1"> 
    <div class="hm-com-W">
        <div class="hmc-rows">
            <div class="hm-area" area-id="Area1"> 
                <div class="hm-area-con">
                    // 这里是其他控件
                </div>
            </div>
            <div class="hm-area" area-id="Area2"> 
                <div class="hm-area-con">
                    // 这里是其他控件
                </div>
            </div>
        </div>
    </div>
</div> 
```

> 就是说，与编辑时相同。多留几层 DIV 是为了做皮肤时，多几层元素容易做出炫酷一点的效果


