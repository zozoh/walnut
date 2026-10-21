---
title:文字编辑控件
author:zozoh
---

# 如何创建实例

```
new ZEditorUI({
    // 编辑器标题。如果不声明，则默认根据 contentType 显示
    title : '<..>'

    // 编辑器处理内容的类型
    // 默认为 text  (所有未知类型，都认为是默认类型)
    contentType : 'text|html..'
});
```

# 控件方法

## getData

```
var text = uiEditor.getData();
```

## setData

```
uiEditor.setData("xxxxx");
```

## setHtml

```
uiEditor.setHtml("<xxxxx>");
uiEditor.setHtml($(..));
uiEditor.setHtml(<..>);
```

* 控件会根据自己给定的类型，对于 HTML 进行处理
* 传入的参数也可以是一个 Element 或者 jQuery 对象

## getHtml

```
var html = uiEditor.getHtml();
```

* 控件会根据自己给定的类型，将数据处理成 HTML 并返回

