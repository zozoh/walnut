# 过滤器简介

`@set` 设置上下文的某个变量

变量值支持下面的格式：

```bash
# Seq(9)
# - prefix = M
# - n = 9
# - seq.parentPath = ~/path/to
# - seq.name = yyyy-MM-dd.'txt'
# - seq.key = my_seq
seq:M:9:~/path/to/yyyy-MM-dd.'txt'#my_seq
> M 000 003 423

# SeqD('FX',9)
seqD:FX:9:~/path/to/yyyy-MM-dd.'txt'#my_seq
> FX 240501 000 005 902

# SeqHH('BMH',5)
seqHH:BMH:5:~/path/to/yyyy-MM-dd.'txt'#my_seq
> BMH 24050109 05 902

# snowQ('XT',4)
snowQ:XT:4
> XT lvwjlrvf A8mz

# snowQD('K',4)
snowQD:K:4
> K ynapsa57 tN7f9r

# uu32
uu32
> 7l075gcbreii6orfl8lh227k3d
```

# 用法

```bash
sqlx @set 
  [varName]           # 变量名称
  [varValue]          # 变量值
  [-to list|map|all]  # 为列表还是map设置，如果不指定，则表示 all
```


