---
title: 布局控件·语法·JSON
author:zozoh
---

----------------------------------
# 布局语法·概述

```js
{
    type : 'rows',
    list : [{
        name : 'sky',
        size : '40rem',
        uiType : 'ui/xx/xx',
        uiConf : {/*..*/}
    }, {
        type : 'cols',
        list : [{
            name : 'chute',
            size : '30%',
            collapseSize : '20px',
            collapse : true
        },{
            bar : true,
        },{
            type : 'tabs',
            action : [/*..*/],
            list : [{
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
        title : {
            icon : '<i...>',
            text : 'i18n:xxx',
            action : [/*..*/],
        },
        pos : {
            dockX : "left",
            dockY : "top",
            width  : "30%",
            height : "90%",
            right  : 0,
            bottom : "40px"
        },
        uiType : "ui/xx"
    }],
}
```

- 支持的布局划分为 `rows|cols|tabs|` 表示水平划分和垂直划分




