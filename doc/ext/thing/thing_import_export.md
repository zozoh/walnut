---
title:Thing的导入导出
author:zozoh
tags:
- 系统
- 扩展
- thing
---

----------------------------------
# 导入导出概述

对于一个 ThingSet 我们提供如下的导入导出功能

- 元数据导入
- 元数据导出
- 完整导入
- 完整导出

----------------------------------
# 元数据导入

## 导入的组合命令

```
sheet id:{{f.id}} -mapping ~/.sheet/测试数据_import -tpo json \
    | thing {{tsId}} create -fields -unique phone
# 这个命令的上下文模板:
{
    f : {..}        // 临时文件对象，一个 WnObj
    tsId  : ID      // 数据集的 ID
    query : ".."    // 导出时给定的过滤参数，要拼在 thing query 后面
}
```

- `sheet` 命令使用详情参看 `man sheet`
    + 也可以直接将这个文件的内容
- `-json` 表示输出的是一个 JSON 数组，以便后续命令 `thing create` 读入
- `thing create -unique` 表示数据用 `phone` 字段去除重复，如果发现重复就替换，而不是新增

---------------------------------------
# 映射文件

```
th_nm:名称
phone:电话
lm[$date%yyyy-MM-dd HH:mm:ss]:最后修改日期
lbls[$n]:标签
th_media_list[$n.id]:媒体
sex[${1=男;2=女}]:性别
th_enabled[$boolean]:生效 
th_enabled[$boolean->Yes/No]:生效
生效[$boolean<-Yes/No]:th_enabled
phone[$str]:电话
email[$str=未设置]:电子邮件
th_price[$int]:价格
th_price[$int=-1]:价格
```

- 字段之间可以用半角逗号分隔，也可以用换行符分隔
- `$date` 表示日期，`%` 后面的表示日期格式，不写的话，默认 `yyyy-MM-dd HH:mm:ss`
- `$n` 表示数组，会用半角逗号连接
- `$n.id` 表示对象数组，输出的值为每个对象的 `id`，用半角逗号连接
- `${1=男;2=女}]` 这样的写法表示一个映射，将 `1` 映射为 `男`
- 所有的 `$xxx` 都可以写成 `@xxx` 譬如 `lbls[@n]:标签` 也是合法的
    - 这是为了有的时候怕 `$` 字符被某些程序处理

Key 的全部写法为：

- 子对象   :  `a.b.c`
- 多重获取 : `key1||key2`
- 数组    : `key[$n]`
- 数组值   : `key[$n.name]`
- 日期    : `key[$date]`
- 日期    : `key[$date%yyyy-MM-dd HH:mm]`
- 布尔（转字符串）: `th_enabled[$boolean->Yes/No]:生效`
- 布尔（来自字符串）: `生效[$boolean<-Yes/No]:th_enabled`
- 字符串 : `phone[$str]:电话`
- 整数 : `th_price[$int]:价格`

## 导入的界面

```
用户选择菜单命令
    |
    V
打开选择文件向导
    |
    V
上传文件至 ThingSet 临时目录: ThingSet/tmp/upload_UUID.xls 「1天的有效期」
    |
    V
执行导入命令 : thing.js 里面配置的命令
    |
    V
拿到命令执行结果，更新界面或者报告错误
```

----------------------------------
# 元数据导出

## 导出的组合命令

```
thing {{tsId}} query <%=query%> | sheet -out id:{{f.id}} -process
# 这个命令的上下文模板:
{
    f : {..}        // 临时文件对象，一个 WnObj
    tsId  : ID      // 数据集的 ID
}
```

- `sheet -f` 表示输出到一个文件里
- `sheet -process` 表示输出的同时向标准输出写入进入

## 导出的界面

```
用户选择菜单命令
    |
    V
收集用户导出信息：页码范围，文件格式(csv,json)，导出文件名
    |
    V
在tmp目录，生成临时导出结果目录(UUID，1天有效)，并生成目标文件名
    |
    V
条用命令进度面板，执行命令
    |
    V
执行完毕后，显示下载链接
```

----------------------------------
# 完整导入导出

## 导入导出包结构

无论是导入还是导出，目标文件都是一个压缩归档包。首先是 zip，以后可以这吃 tgz 等格式

```bash
# import_20181204a.zip
meta.json     # 也可以是 csv, 或者 xls，优先级为 json>csv>xls
mapping.js    # 字段映射
data/         # 数据
    13910110053/     # 以某个唯一性字段为目录
        detail       # 详情信息
        media/       # 媒体目录
            a.jpg    # 媒体文件
        attachment/  # 附件目录
            x.zip    # 附件
```

## 完整导入

*TODO 细节待定*

## 完整导出

*TODO 细节待定*








