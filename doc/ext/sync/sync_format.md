---
title: sync·数据结构
author: wendal
---

# 配置信息的存储结构

配置信息使用thing作为基础结构

## 目录结构

```bash
/home/$user          # 域目录
  |- .sync/          # 所有的数据存放
    |- index/    # 配置索引目录
        |- $ID   # 每个配置是一个记录
    |- data/     # 数据目录
        |- $ID/  # 配置的id号
            |- tindex/   # 备份时的索引数据
            |- tpkg/     # 备份数据的压缩包
```

## 元数据

```bash
th_nm      : "备份定位数据",                      # [必]备份配置的名称
basepath   : "~",                                # [选]备份的根目录,需要备份的文件夹都必须在该目录之下,默认为用户主目录
includes   : ["设备列表/", "定位数据/", ".gpt$"] , # [选]需要包含的文件路径前缀
                                                  # 如果以^或$结尾代表正则表达式
                                                  # 如果为空数组,默认为basepath下所有非隐藏目录
excludes   : [".tmp$"],                          # [选]需要排除的文件路径前缀,如果以^或$结尾代表正则表达式, 默认为空
plans      : ["0 0 4 * * *"],                    # [选]备份计划, 为cron表达式
before     : "~/.jsbin/check_disk_usage.js",     # [选]前置执行的脚本
after      : "~/.jsbin/send_notify_mail.js",     # [选]后置执行的脚本
timelimit  : ["-1week", "now"]                   # [选]时间范围, 过滤以文件/文件夹的最后修改时间为准
                                                    # 默认是没有过滤时间区间,全备份
                                                    # 正整数,代表该时间戳之后
                                                    # 负整数,代表区间为: [(当前时间-毫秒数), 当前时间]
                                                    # 字符串 "-1week" 代表最近一周, "-1month"代表最近一个月
```

## 索引数据文件(位于tindex目录)

数据以行为单位, 每行以\n结尾. 文件命名默认是当前时间戳

* 第一行为特殊行,以#开始,存储备份的元数据信息
* 剩余数据行的结构以逗号分隔各数据段, 每行代表一个文件或文件夹

```
# {...}
$id,$race,$ph,$lm,$sha1
$id,$race,$ph,$lm,$sha1
$id,$race,$ph,$lm,$sha1
```

第一行的数据结构

```bash
{
    basepath : "/home/wendal" # 备份的根目录
}
```

数据行的各数据段含义

```bash
$id   : "abc...def"    # 文件/文件夹的唯一id
$race : "dir"          # 类型, dir文件夹, file文件, link链接
$ph   : "points/xx/ayc"# 相对于备份根目录(basepath)的相对路径
$lm   : 15602342342    # 最后修改时间
$sha1 : "utf...tgp"    # 文件指纹, 仅race=file时有意义
```

## 数据文件(位于tpkg目录)

数据文件的命名, 默认为时间戳, 后缀以选定的压缩格式对应.

例如zip压缩, 则以.zip结尾, 使用tar+gzip压缩, 则以.tgz结尾

通用文件结构如下

```bash
- / 
    - obj/  # 元数据
        - $id
        - $id
        - $id
    - data  # 文件数据
        - $sha1
        - $sha1
```