---
title: 布局控件·语法·XML
author:zozoh
---

----------------------------------
# 布局语法·概述

```xml
<?xml version="1.0" encoding="UTF-8"?>
<layout>
    <!--
    rows 分栏，将界面水平划分为多行
     @size 表示显示的宽度，支持 `px|%|rem|em` 等 css 尺寸单位。只有数字的话表 px
           不声明 @size 的区域表自动应用剩余宽度
    -->
    <rows>
        <row name="sky" size="40rem">
            <!--
            声明本行的默认控件，控件的 type 为必选，是控件类型。
            内容 CDATA 为控件的配置信息
            -->
            <ui type="ui/xxx/xxx"><![CDATA[
            {
                // 这里是控件配置的 JSON
            }
            ]]></ui>
        </row>
        <row>
            <cols>
                <!--不指定 <ui> 则表示没有默认控件，需要创建时指定-->
                <col name="chute" size="30%" collapse-size="20px" collapse="yes"/>
                <!--插入调整条，则表示调整前后两个区域-->
                <bar/>
                <col>
                    <!--
                    标签组表示组合一组标签。每个标签对应一个控件
                    标签组可以声明一个 <action> 表示对整个标签操作
                    实际上，它的实现会利用 tabs 控件来完成
                    -->
                    <tabs>
                        <action><![CDATA[{..这里是menu的配置..}]]></action>
                        <!--
                        每个标签都可以声明 icon/text 以及一个默认 UI
                        当然，这个UI定义也是可以在稍后在创建实例时被覆盖的
                        -->
                        <tab name="c0">
                            <icon><![CDATA[<i class="fa fa-user"></i>]]></icon>
                            <text>i18n:xxxx</text>
                            <ui type="ui/xxx"/>
                        </tab>
                    </tabs>
                </col>
                <col name="brief"><!--如果没写UI则在创建实例时读取--></col>
            </cols>
        </row>
        <row name="footer" size="40rem">
            <ui type="ui/xxx/xxx"/>
        </row>
    </rows>
    <!--
    box 存放在布局的根部，表示隐藏区域，可以通过方法 showArea/hideArea/toggleArea
    实现的时候，实际上只有显示 box 的时候，才会绘制对应的 UI

    另外 box 内部也可以组合 rows/cols/tabs
    -->
    <box name="xx">
        <!-- 有标题栏，则可以承载一个统一的 actions -->
        <title>
            <icon><![CDATA[<i class="fa fa-user"></i>]]></icon>
            <text>i18n:xxxx</text>
            <action><![CDATA[..这里是menu的配置（数组）..]]></action>
        </title>
        <!--
        默认的 box 位置和尺寸，当用户拖动时，位置信息会存放在本地
        @dock-x 和 @dock-y 决定了要采用哪些属性。这两个值一定是有的
        如果没有，默认left/top
        拖动的时候，会判断 box 中心点哪个角近，就选择哪个
        -->
        <pos dock-x="left" dock-y="top">
            <width>30%</width>
            <height>90%</width>
            <right>0</right>
            <bottom>40px</bottom>
        </pos>
        <!--默认UI咯，你懂的-->
        <ui type="ui/xx"/>        
    </box>
</layout>
```

- 支持的布局划分为 `rows|cols|tabs|` 表示水平划分和垂直划分




