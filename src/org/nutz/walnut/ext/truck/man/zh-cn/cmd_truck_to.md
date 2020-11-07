过滤器简介
======= 

`@from` 设置转换目标，同时确定转换模式

- `INDEXER` 仅转换索引
- `BM` 仅转换桶
- `BOTH` 转换索引和桶
 

用法
=======

```bash
@to 
  [~/path/to]        # 【选】目录路径，默认采用 @from 的路径
  [-index dao(abc)]  # 指明索引映射方式，默认采用目录上的原始设定
  [-bm lbm(Tmp)]     # 指明桶管理器，默认采用目录上的原始设定
```

- `@to` 必须指定 `index` 或者 `bm`，才会转换
- 如果仅指定 `index` 则只转换索引， `bm` 同理


示例
=======

```bash
truck @from ~/account @to -index dao(abc)
```

