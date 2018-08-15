---
title: 布局控件·语法·JSON
author:zozoh
---

----------------------------------
# 布局语法·JSON·概述

```js
{
    key  : 'xxx',
    type : 'rows',
    rows : [{
        name : 'sky',
        size : '40rem',
        uiType : 'ui/xx/xx',
        uiConf : {/*..*/}
    }, {
        type : 'cols',
        cols : [{
            name : 'chute',
            size : '30%',
            collapseSize : '20px',
            collapse : true
        },{
            type : "bar"
        },{
            type : 'tabs',
            action : [/*..*/],
            tabs : [{
                name : 'c0',
                icon : '<i...>',
                text : 'i18n:xxx',
                uiType : 'ui/xx/xx',
                uiConf : {/*..*/}
            }]
        },{
            name : 'brief'
        }]
    }, {
        name : 'footer',
        size : '40rem',
        uiType : 'ui/xx/xx',
        uiConf : {/*..*/}
    }],
    boxes : [{
        type  : 'box',
        title : {
            icon : '<i...>',
            text : 'i18n:xxx',
            action : [/*..*/],
        },
        pos : {
            dockAt : "P",
            width  : "30%",
            height : "90%",
            right  : 0,
            bottom : "40px"
        },
        // 自己的内容，可以是 type = rows 或者 uiType
        uiType : "ui/xx",  // 这个更优先
        box : {
            type : "rows",
            rows : [/*..*/]
        }
    }],
}
```

- 支持的布局划分为 `rows|cols|tabs|` 表示水平划分和垂直划分




