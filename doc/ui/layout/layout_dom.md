---
title: 布局控件·语法·HTML
author:zozoh
---

----------------------------------
# 布局语法·HTML·概述

一个布局会本最终渲染成下面的样子:

```html
<div class="ui-arena">
    <div wl-type="rows">
        <div class="wn-layout-con">
            <div ui-gasket="sky" wl-size="40rem"><!--嵌入UI--></div>
            <div wl-type="cols">
                <div class="wn-layout-con">
                    <div ui-gasket="chute" wl-size="30%" 
                         wl-collapse-size="20px" collapse="yes"><!--嵌入UI--></div>
                    <div wl-type="bar"></div>
                    <div wl-type="tabs">
                        <div class="wlt-tabs">
                            <ul>
                                <li wl-tab-name="c0">
                                    <i class=""></i><span>xxx</span></li>
                                <li wl-tab-name="c1">
                                    <i class=""></i><span>xxx</span></li>
                            </ul>
                            <div class="wl-action" 
                                 ui-gasket="action_menu_0"><!--嵌入menu--></div>
                        </div>
                        <div class="wn-layout-con">
                            <div ui-gasket="c0"><!--嵌入UI--></div>
                            <div ui-gasket="c1"><!--嵌入UI--></div>
                        </div>
                    </div>
                    <div ui-gasket="brief"><!--嵌入UI--></div>
                </div>
            </div> <!--// End of <div wl-type="cols">-->
            <div ui-gasket="footer" wl-size="40rem"></div>
        </div> <!--// End of <div class="wn-layout-con">-->
    </div>
    <div wl-type="box"><div class="wlb-con">
        <div class="wlb-info">
            <div class="wlbt-title">
                <i class="fa fa-user"></i>
                <span>xxxxx</span>
            </div>
            <div class="wl-action"
                 ui-gasket="action_menu_1"><!--嵌入menu--></div>
        </div>
        <div class="wlb-main"><!--
            可以嵌套更多复杂的 rows/cols/tabs
            也可以声明 ui-gasket 表明一个 UI
        --></div>
    </div></div>
</div> <!--// End of .ui-arena-->
```





