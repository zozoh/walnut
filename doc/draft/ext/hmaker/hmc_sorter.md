---
title:控件:排序器控件
author:zozoh
tags:
- 扩展
- hmaker
---

# 编辑时 DOM 结构

```
<div class="hm-com hm-com-sorter" id="排序条件"> 
<div class="hm-com-W">
<div class="hmc-sorter hmc-cnd">
    <ul> 
        <li key="price" or-val="1" or-nm="asc">
            <em>价格</em><span or-icon="asc"></span>
        </li> 
        <li key="lm" modify="yes" or-val="-1" or-nm="desc">
            <em>最后修改时间</em><span or-icon="desc"></span>
        </li> 
    </ul>
</div>
</div>
</div> 
```

# 输出时 DOM 结构

```
<div class="hm-com hm-com-sorter hmc-sorter" id="sorter_1"> 
    <ul> 
        <li key="price" or-val="1" or-nm="asc">
            <em>价格</em><span or-icon="asc"></span>
        </li> 
        <li key="lm" modify="yes" or-val="-1" or-nm="desc">
            <em>最后修改时间</em><span or-icon="desc"></span>
        </li> 
    </ul> 
</div>
```

# getComValue

永远返回字符串:

```
"nm:1"           // 单个排序 1:ASC, -1:DESC
"ct:1,lm:-1"     // 多个排序条件用半角逗号分隔
}
```

# 运行时获取值

```
var re = $(xxx).hmc_sorter("value");
```

> 这里返回值 re 的对象结构与 [getComValue的返回值](#getComValue) 相同

# 运行时设置值

```
$(xxx).hmc_sorter("value", "ct:1,lm:-1");
```

> 这里新值的的对象结构与 [getComValue的返回值](#getComValue) 相同


