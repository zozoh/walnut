---
title: 折叠项目控件
author:zozoh
---

# 控件的调用方法

让当前选区所有子元素超出选区的部分折叠在一起

```
jQuery.folder({
    itemIcon  : null,             // 折叠后，项目的图标 HTML
    itemTag   : "DIV",            // 折叠后，项目的元素标签
    itemText  : "...",            // 折叠后，项目的文字标识
    itemClass : "folder-item",    // 折叠后，项目的选择器名称
    dropClass : "folder-drop",    // 折叠后，展出菜单的名称
    mode      : "head",           // 是从头部折叠还是尾部折叠 head|tail
    keep      : 0                 // 折叠的时候保留之前（后）多少个元素
});
```

