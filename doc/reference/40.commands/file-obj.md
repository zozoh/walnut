---
title: 文件对象命令组
module: impl.box.cmd
keywords: [ls, cat, cp, mv, rm, find, o, mount, touch, mkdir, ln, 文件操作]
source:
  - impl/box/cmd/
  - ext/data/o/
related: [everything-is-obj, io-model, mount-mapping, command-model]
---

# 文件对象命令组

对树形对象空间的日常操作。机制见 [io-model](../20.io-storage/io-model.md)，字段含义见 [everything-is-obj](../10.concepts/everything-is-obj.md)。

## 浏览与读写

```bash
ls ~                      # 列目录
ll ~/docs                 # 详细列表（含元数据）
tree ~/docs               # 树形展示
cat ~/a.txt               # 读内容
echo "hi" > ~/a.txt       # 写（重定向）
cat ~/a.txt >> ~/b.txt    # 追加
du ~/docs                 # 统计大小
dircount ~                # 子项计数
disk                      # 配额/用量
```

## 创建与删除

```bash
mkdir -p ~/docs/sub       # 建目录
touch ~/docs/new.txt      # 建空文件 / 刷新时间
rm ~/docs/old.txt         # 删除
rm -r ~/docs/sub          # 递归
cp ~/a.txt ~/docs/        # 复制
mv ~/a.txt ~/docs/b.txt   # 移动=重命名
ln -s ~/docs ~/alias-docs # 链接对象
```

## find：按条件查对象

```bash
find ~ -name '*.jpg'                # 按名
find ~ -type f -mtime -1            # 一天内修改
find ~ -match 'lbls:"vip"'          # 元数据匹配（同 o -match）
find / -match 'tp:"wn_payment"' -limit 50
```

## o：元数据操作主力

> 旧的内置 `obj` 命令仍在（`impl/box/cmd/cmd_obj.java`），但已基本弃用，请统一使用 `o`（`ext/data/o/`）。

```bash
# 查询输出
o ~/a.txt -cqn                    # 全部元数据
o ~/a.txt -t 'id,nm,sha1,len' -bish   # 指定字段表格

# 修改
o ~/a.txt -u 'brief:"新摘要",lbls:["x"]'
o ~/a.txt -renm 'new-name.txt'    # 重命名（也移动路径）

# 批量模板（生成命令交给 run 执行）
o /domain -tmpl 'o id:@{id} -u \'tp:"A"\';' | run

# 唯一键计数器
o ~/counters/orders -inc 'nb:1' -quiet   # nb 原子 +1
```

增强对象操作在 `o` 命令族（39 个 `@子命令`，见 `man o`），写法与示例详见 [o 命令](o.md)：

```bash
o ~/orders @query '{status:"NEW"}' -sort '{ct:-1}' -limit 10 -l
o ~/orders/4k2.. @get -cqn
o ~/orders/4k2.. @update 'status:"OK"'
o ~/orders @count '{status:"NEW"}'
o ~/orders @create '{nm:"order_9",status:"NEW"}'
```

## 挂载

```bash
mount ~/data 'dao(demo/t_news)'     # 设置映射
umount ~/data                        # 移除
o ~/data -cqn                      # 映射目录照常操作
```

## 打包与导出

```bash
zip ~/backup.zip ~/docs/            # 打包
unzip ~/backup.zip -d ~/restore/     # 解包
unzipx ~/backup.zip -view           # 预览包内容
archive ...                          # 归档体系（man archive）
```

## 场景速查

- **「这个对象是什么」**：`o <path/id> -cqn`
- **「按标签找图」**：`o ~/photos -match 'lbls:"trip",tp:"jpg"'`
- **「批量改元数据」**：`o ... -tmpl '...' | run`
- **「目录瘦身」**：`find ~ -size +100m | xargs rm`
- **「建测试数据」**：见 [data-cmd](data-cmd.md) 的 fake

## 注意事项

- 路径支持 `~`、`id:xxx`、`type:xxx`、绝对路径混用。
- `rm` 直接物理删（回收站见 `trash` 命令）。
- 大对象操作用 `id:` 引用，避免路径变更导致的竞态。
