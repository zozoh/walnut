---
title: 动态网页(wnml)语法
author:zozoh
tags:
- 系统
- 扩展
- www
---

# 网页模板语法

网页模板就是普通的 HTML，只不过支持一些自定的标签和属性

## 全局上下文

```
// 以下是每个网页模板都会得到上下文，这个上下文会被数据源扩展
{
    grp : "xxx"       # 提供网页服务器的 Walnut 用户组名
    fnm : "xxx"       # 网页的文件名
}
```

## 数据源

数据源可以对上下文进行补充

```
<script class="wn-datasource" name="obj" type="json">
obj ~/abc/*
</script>
```

* 数据源通过 `<script>` 标签，申明一段命令
* 如果 *type=json* 那么会认为命令的输出是一段 JSON 字符串，可以是对象也可以是列表
* 还支持的类型 *type=string* 表示输出的就是字符串
* 这个对象将根据 *name* 属性记录在上下文中
* 在输出到浏览器时，数据源节点将被移除
* 数据源按照声明的先后顺序逐次执行，后面的数据源会覆盖前面的数据内容
* 全局上下文的内容是最优先的，会覆盖数据源输出的上下文（如果重名的话）

## 静态引入 `<include>`

```
<include path="/path/to/file"/>
```

* 可以写在网页的任意地方，它都将引入目标文件
* 目标加入 DOM 后，将统一执行替换

## 文本占位符

```
<div class="my_${obj.type}">
    文本任意地方都可以插入占位符 ${pos.x} 
</div>
```

* 只有属性和文本节点支持占位符
* 占位符支持 `${obj.type<类型:格式>?默认值}` 的写法

## 判断 `<if>`

```
<if test="$EL">
    只有 $EL 为 true 的时候，下面的 DOM 才会被渲染，否则整个节点会被移除
</if>
```

## 循环 `<each>`

```
<each var="abc" items="obj.list">
    会在上下文中创建名为 abc 的对象，等退出循环就会删除掉
    如果 obj.list 不是一个集合或者数组，那么会当做对象循环一次
    其内的 DOM 节点会被循环输出
</each>
```

## 分支 `<choose>`

```
<choose>
    <when test="$EL">
        $EL 为真的时候输出其内内容
    </when>
    <otherwise>
        所有的 when 都不满足的时候，输出这里的内容
    </otherwise>
</choose>
```

## 重定向 `<redirect>`

```
<if test="$EL">
    <redirect code="302" text="Found">/path/to/new</redirect>
</if>
```

重定向支持下面几个状态码，如果你写了其他的状态码，可能会出现不可预知的错误。
如果你不写 *text* 属性，那么输出的响应头就是下面这些默认值。
如果你甚至都不写 *code* 属性，那么默认就是 *302*

* 301 Moved Permanently
* 302 Found
* 303 See Other (since HTTP/1.1)
* 304 Not Modified 
* 305 Use Proxy (since HTTP/1.1)
* 306 Switch Proxy
* 307 Temporary Redirect (since HTTP/1.1)
* 308 Permanent Redirect


