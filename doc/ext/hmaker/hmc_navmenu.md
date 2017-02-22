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
        <li class="li-top" current="yes" open-sub="yes" sub-item-nb="1">
            <a target="_blank"><i></i><span>首页</span></a>
            <ul class="ul-sub ul-sub-0">
                <li class="li-sub li-sub-0" sub-item-nb="3">
                    <a target="_blank"><i></i><span>产品</span></a>
                    <ul class="ul-sub ul-sub-n">
                        <!--// 这里是二级菜单 -->
                    </ul>
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

- `sub-item-nb` 属性表示本菜单项有多少个子项目，没有子菜单的话，没有这个属性
- `open-sub` 表示当前菜单是否是打开了子菜单。以便皮肤提供者编辑皮肤
    + 注意，这里有个小坑。 编写 css 选择器时，记得用 `>` 限制一下，否则子菜单的子菜单也会受影响
- `current` 表示当前菜单项是否要高亮

# 输出时 DOM 结构

```
<div class="hm-com hm-com-navmenu hmc-navmenu" id="导航菜单"> 
<div class="hmc-navmenu">
    <ul class="mic-top ul-top">
        <li class="li-top" current="yes" open-sub="yes" sub-item-nb="1">
            <a target="_blank"><i></i><span>首页</span></a>
            <ul class="ul-sub ul-sub-0">
                <li class="li-sub li-sub-0" sub-item-nb="3">
                    <a target="_blank"><i></i><span>产品</span></a>
                    <ul class="ul-sub ul-sub-n">
                        <!--// 这里是二级菜单 -->
                    </ul>
                </li>
            </ul>
        </li>
        <li class="li-top">
            <a target="_blank"><i></i><span>关于</span></a>
        </li>
    </ul>
</div> 
```

