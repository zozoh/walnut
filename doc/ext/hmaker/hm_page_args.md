---
title:页面路径参数
author:zozoh
tags:
- 扩展
- hmaker
---

# 缘起

> Issue: https://github.com/zozoh/walnut/issues/448

通常我们访问一个动态页面

```
http://mysite.com/news_show.html?id=th9iviij1shprrc65i9fu1db0e
```

但是据说将这个链接变成`伪静态`页面（让搜索引擎认为是静态页面）会对 SEO 有好处，即，访问

```
http://mysite.com/news_show_th9iviij1shprrc65i9fu1db0e.html
```

# 解决方案

## hMaker 的支持

首先, hMaker 的编辑面板，原来的

```
@<id>th9iviij1shprrc65i9fu1db0e
会编译成
${params.id?th9iviij1shprrc65i9fu1db0e}
```

仿照这个增加一个语法

```
%<id>th9iviij1shprrc65i9fu1db0e
会编译成
${args.id?th9iviij1shprrc65i9fu1db0e}
```

那么这个 `args` 是从哪里来的呢？

在 `hmaker publish` 会判断每个页面，如果是下面这种形式的

```
abc_{{id}}
```

则会给目标页面标识一个属性 `hm_pg_args:true`

## 在 WWW 模块的支持

如果页面标识了属性 `hm_pg_args:true`，那么则表示这个页面的名称带有动态占位符
譬如 `abc_{{id}}.html`
渲染页面是，会依次匹配这个动态占位符，并将其转换为一个 Map
存放在渲染上下文的 `args` 中












