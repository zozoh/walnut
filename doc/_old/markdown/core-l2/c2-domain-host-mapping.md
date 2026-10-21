---
title: 域名映射机制
author: zozohtnt@gmail.com
key: c2-dmn
---

# 记录元数据

```bash
nm : "www.demo.com"      # 域名
#------------------------------------------
domain  : "demo"         #【必】映射到的目标域
title   : "xxx"          #【选】一个助记名 
#------------------------------------------
# 记录类型，A: 网站, B: 仅获取域映射
# 不可识别的记录类型或者是空类型，均当作 "A"
tp : "A"
#------------------------------------------
# 有效期（绝对毫秒数）
# > 不采用 expi 的原因是，当记录过期后，还希望保留一下
# > 如果实在需要删除，做个清理任务定期清理比较好
expi_at : AMS
#------------------------------------------
# 【选】直接指定站点目录
# 如果不指定，则会在目标域进行一次查询（匹配 www:"..."）
# 这会比较耗时
site    : "~/www/user"
```

> 迁移旧的元数据指令为：

```bash
obj * -tmpl 'obj id:@{id} -u \'tp:"A",title:"@{nm}",nm:"@{dmn_host}",domain:"@{dmn_grp}",expi_at:@{dmn_expi}\';' | run
```

> 如果想看到详细列表，命令为

```bash
obj /domain -match -t 'id,nm,tp,domain,site,expi_at,title' -bish -pager -limit 100 -skip 0
# or alias command:
domain list -limit 100 -skip 0
```