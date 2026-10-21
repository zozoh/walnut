---
title: 缩略图机制
author: zozoh
tags:
- 系统
- 缩略图
---

# 缩略图的存放

```
// 对象的元数据将存放缩略图的 ID 或者路径
{
    thumb : "id:34cd.."   // 或者 "/path/to/thumb"
}

// 对应的缩略图文件，它有对应文件的属性
{
    thumb_src : "id:df23.."  // 或者 "/path/to/thumb-src"
}
```

通常系统存放缩略图的位置:

```
// 生成的缩略图存放位置
~/.thumbnail/gen/${srcId}

// 默认缩略图存放位置
~/.thumbnail/dft/


// 系统缩略图存放位置
/etc/thumbnail/
```

* 默认缩略图和系统缩略图，需要根据 `obj.tp` 来读取
    - 他们都在文件夹下
    - 他们都是 *png* 文件名格式都类似 *128x128* 
* 如果 `obj.tp` 为空，对于 *DIR* 就认为是 `folder`，*FILE* 则是 `unknown`

## 缩略图读取接口

为了便于读取缩略图，系统 ObjModule 提供了读取接口

```
/o/thumbnail/**

路径参数 : id:xxx 或者 /path/to/obj 或者 type:xxxx

GET参数:
w : 64      # 【选】指明缩略图的宽度
h : 64      # 【选】指明缩略图的高度

响应: 
HTTP 200  就是标准的HTTP响应留，可以用 <img> 等标签直接显示
HTTP 404  对象不存在
```

* 如果对象存在一定会返回一个缩略图的。
* 如果对象不存在 `thumb` 元数据，则返回默认的相关类型的缩略图，
* 默认的缩略图 MIME一定是 `image/png`，即透明png24表示的图标
* 不指明尺寸，系统将会自行决定返回什么尺寸的图像

比如:

```
// 直接读取某对象缩略图(64x64)
/o/thumbnail/id:10n8qk1m7ui5foqb5f7ejhoqs5?sh=64

// 直接读取 folder 类型的缩略图(32x32)
/o/thumbnail/type:folder?sh=32
```

## 自动生成缩略图

系统提供命令 `chimg`，结合钩子，可以为图片类型的文件自动生成缩略图






