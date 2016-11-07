---
title  : HMaker 总体设计
author : zozoh
tags:
- 扩展
- hmaker
---


# 消息系统

hmaker 的子 UI 可以监听下列通知:

 Message         | Params        | Comments 
-----------------|---------------|----------
active:rs        | o             | 选中资源项目
active:folder    | o             | 文件夹被激活
active:other     | o             | 其他对象被激活
active:page      |               | 页面被激活
active:block     | jBlock        | 块被激活
active:area      | jArea         | 栏被激活
active:com       | jCom          | 组件被激活
change:block     | {..}          | 块被修改
change:area      | {..}          | 栏被修改
change:com       | {..}          | 组件被修改
change:com:ele   | {..}          | 组件内元素被修改后
show:com:ele     | --            | 需要显示控件的扩展属性面板
hide:com:ele     | --            | 需要隐藏控件的扩展属性面板
active:file      | o             | 文件夹视图里文件被激活
blur:file        |               | 文件夹视图里文件被取消激活
reload:folder    |               | 通知文件夹视图重新刷新自己的子节点
change:site:skin |               | 站点的皮肤发生改变



