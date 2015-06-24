---
title:钩子的机制
author:zozoh
tags:
- 系统
- 钩子
---

# 钩子怎么声明

```
$HOME/.hook                   # .hook 文件夹存放所有的钩子
    create                    # 当创建完一个对象后调用
        init_box_folder       # 命令模板文件
    write                     # 当对象被修改后调用
        make_thumbnail.js     # 一个JS脚本，描述调用后逻辑

    delete                    # 当对象被删除前调用
    move                      # 当对象被移动前调用
    rename                    # 当对象被改名前调用
    meta                      # 当对象元数据被修改后调用
    mount                     # 当对象被设置挂载后调用
    type                      # 当对象类型被修改后调用
```

每个钩子文件都支持元数据:

```json
{
    ..
    hook_by : [{                  // 钩子条件数组是 "OR" 的关系
        type : "^jpe?g|png$",       // 正则表达式表示特殊类型符合就会触发
        path : "^/home/xiaobai/.*$" // 正则表达式表示特殊路径
        ...
        //任何 WnObj 的元数据都可以在这里声明
        //这些条件是 "AND" 的关系
        ...
    },{
        ... // 下一个条件 ...
    }]
    ..
}
```

# 命令模板钩子文件

所谓命令模板，就是文件的每一行都是一条命令，会被依次执行。
其中每一行的命令格式类似:

```
cat ${id} | grep ${ph}
```

* 模板的占位符就是对象的元数据字段
* 基本上和 [httpapi](httpapi.md) 的命令占位符一样

# js 钩子文件

为了能支持更复杂的逻辑，钩子可以用 JS 来编写。
脚本执行时，当前操作的对象会被保存在上下文里，通过全局变量 *obj* 即可访问。
具体 *obj* 提供了哪些属性，请参看 `org.nutz.walnut.impl.io.WnBean` 的实现。
它就是一个 Map，每个字段都有它自己的缩写

同时，为了提供 IO 等系统访问能力，全局变量 *sys* 也会被提供。 它提供如下接口:

```js
sys.me               // WnUsr
sys.se               // WnSession
sys.io               // WnIo
sys.exec("...")      // 执行系统命令的接口
```

```js
if(obj.tp == "jpg"){
    sys.exec("chimg 800x600 " + obj.id);
}
```











