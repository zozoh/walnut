---
title:控件:过滤器控件
author:zozoh
tags:
- 扩展
- hmaker
---

# 编辑时 DOM 结构

```
<div class="hm-com hm-com-filter" id="搜索条件"> 
<div class="hm-com-W">
    <div class="hmc-filter hmc-cnd">
        <div class="hmcf-list"> 
            <div class="hmcf-fld" key="price"> 
                <span class="fld-info"><em>价格</em></span> 
                <ul> 
                    <li it-type="number_range" it-value="(,10)">
                        <span>白菜价</span>
                    </li> 
                    <li it-type="date_range" it-value="[2015-12-12,)">
                        <span>面向工薪</span>
                    </li> 
                    <li it-type="string" it-value="随便什么值">
                        <span>小资</span>
                    </li> 
                </ul> 
                <span class="fld-multi"><b>多选</b></span> 
            </div> 
            <div class="hmcf-fld" key="lm"><!--//这是第二个字段--></div> 
            <div class="hmcf-fld" key="dd"><!--//这是第三个字段--></div>  
        </div>
        <div class="hmcf-exts">
            <div class="hmcf-fld" key="lm"><!--//这是第四个字段--></div> 
            <div class="hmcf-fld" key="dd"><!--//这是第五个字段--></div>  
        </div>
        <div class="hmcf-exts"><b msg-show="展开" msg-hide="收起">展开</b></div>
    </div>
</div>
</div> 
```

# 输出时 DOM 结构

```
<div class="hm-com hm-com-filter hmc-filter hmc-cnd" id="搜索条件"> 
    <div class="hmcf-list"> 
        <div class="hmcf-fld" key="price"> 
            <span class="fld-info"><em>价格</em></span> 
            <ul> 
                <li it-type="number_range" it-value="(,10)">
                    <span>白菜价</span>
                </li> 
                <li it-type="date_range" it-value="[2015-12-12,)">
                    <span>面向工薪</span>
                </li> 
                <li it-type="string" it-value="随便什么值">
                    <span>小资</span>
                </li> 
            </ul> 
       </div> 
       <div class="hmcf-fld" key="lm"><!--//这是第二个字段--></div> 
       <div class="hmcf-fld" key="dd"><!--//这是第三个字段--></div>  
    </div>
    <div class="hmcf-exts">
        <div class="hmcf-fld" key="lm"><!--//这是第四个字段--></div> 
        <div class="hmcf-fld" key="dd"><!--//这是第五个字段--></div>  
    </div>
    <div class="hmcf-exts"><b msg-show="展开" msg-hide="收起">展开</b></div>
</div> 
```

# getComValue 返回

永远返回 JSON 对象

```
{
    "字段名" : "34"             // 固定值
    "字段名" : "[12,]"          // 数字范围
    "字段名" : "[,2016-02-21)"  // 日期时间范围
    "字段名" : ["24", "78"]     // 数组则表示用户的多选
}
```


