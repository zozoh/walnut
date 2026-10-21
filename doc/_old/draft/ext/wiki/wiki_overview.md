---
title: Wiki 机制概述
author:zozoh
tags:
- 系统
- 扩展
- wiki
---

-------------------------------------------------------
# Wiki 机制

wiki 机制的设计目标:

1. 本地编译一个 wiki 文件夹为一个 HTML
2. 在 Walnut 里编译一个 wiki 文件夹，并输出到一个指定目录
3. 对 hMaker 环境能给到很好的支持

-------------------------------------------------------
# 触发 wiki 编译的时机

1. 手工触发
2. 监控一个目录，如果发现改动则触发

-------------------------------------------------------
# 编译 wiki tree

1. 可以输出成 `json|xml|html` 格式的 tree
2. Tree 结构可以从下面的地方读取
   - tree.md
   - tree.xml
   - tree.html
   - 文件夹的自然结构（按名称从小到大排序）
3. 从 tree 文件读取并解析出 tree.html，如果遇到了第一个 UL，则表示开始文档集

## tree.md

```
# 这个是标题
---
- 01. **标题A**
  - [**标题A.1**](AA/xxx.md)
  - [**标题A.2**](AA/xxx.md)
- [02. **标题B**](BB/README.md)
  - [02.01 **标题B.1**](BB/xx.md)
  - [02.02 **标题B.2**](BB/xx.md)
```

## tree.xml

```
<?xml version="1.0"?>
<doc title="Nutz核心包" author="zozoh(zozohtnt@gmail.com)">
	<doc path="nutz_preface.md"/>
	<doc path="nutz_release_notes.md"/>
	<doc path="changelog.md"></doc>
	<doc path="basic" title="通用知识">
		<doc path="maven.md></doc>
		<doc path="encoding.md"></doc>
	</doc>
	<doc path="mvc"	title="Mvc 手册">
		<doc path="overview.man"/>
		<doc path="hello.man"/>
	</doc>
</doc>
```

## tree.html

```
<html>
<head>
    <title>其实你可以随便写，无所谓</title>
</head>
<body>
<h1>标题写什么我们都不关心</h1>
<hr>
<div>
    <ul>
        <li><b>标题A</b>
            <ul>
                <li><a href="AA/xxx.md"><b>标题A.1</b></a></li>
                <li><a href="AA/xxx.md"><b>标题A.2</b></a></li>
            </ul>
        </li>
        <li><a href="BB/README.md"><b>标题B</b></a>
            <ul>
                <li><a href="BB/xxx.md"><b>标题B.1</b></a></li>
                <li><a href="BB/xxx.md"><b>标题B.2</b></a></li>
            </ul>
        </li>
        <li>标题C
            <ul>
                <li><a href="CC/xxx.md"><b>标题C.1</b></a></li>
                <li><a href="CC/xxx.md"><b>标题C.2</b></a></li>
            </ul>
        </li>
    </ul>
</div>
</body>
```