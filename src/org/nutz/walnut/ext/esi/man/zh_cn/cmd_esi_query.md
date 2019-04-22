# 命令简介 

    `esi query` 用来查询数据
    
    本命令依赖元数据esi_conf

用法
=======

```
esi $path query 
					[-match 'nm:"*w*"']   # 需要匹配的条件,可以是map, 可以是数组
					[-match_all]          # 当match是数组时,条件之间的关系默认是or, 加本参数可以变成and
					[-w '{xx:xxx}']       # 使用DSL直接编写查询条件, 使用ES原生JSON格式
					[-rawresp]            # 打印原始响应, 不转WnObj对象
					// 下列参数与thing完全一致
                [-pager]
                [-limit 10]
                [-skip 0]
                [-sort "nm:1"]
                [-obj]
```

path 带esi_conf元数据的目录路径

当match条件带星号时, 将作为糊匹配, 否则作为精确匹配

```
-match 'nm:"wendal"' 精确匹配
-match 'nm:"*wen*"'  模糊匹配
-match 'ct:[123,334]' 日期和数值字段支持区间匹配
```

### 示例

```
esi . query -match 'nm:"*w*"' -l -pager -sort ct:-1
# 输出结果与thing query是一样的
{
   list: [{
      id: "r2hrqm3srgjuhrf1formjhki3o",
      ct: 1555904723651,
      lm: 1555904741854,
      nm: "wendal",
      tp: "txt",
      mime: "text/plain",
      pid: "67pd4i5l06gr9o9jqcdvhj15c4",
      esi_hit_score: 1.0 # 多一个额外属性,代表匹配的程度
   }, {
      ...
   }],
   pager: {
      pn: 1,
      pgsz: 500,
      pgnb: 1,
      sum: 3,
      skip: 0,
      nb: 3
   }
}
```