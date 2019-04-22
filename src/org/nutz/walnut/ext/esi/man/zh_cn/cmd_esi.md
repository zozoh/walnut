# 命令简介 

    `esi` 用来处理与elasticsearch服务器的交互
    
    本命令依赖元数据esi_conf
    
```
{
    id : "abc...def",
    esi_conf : {
    	name : "esi_test",    # 索引名称,必须存在. 
                             #实际存储的索引名称是 $user_esi_test,例如wendal用户创建的是 wendal_esi_test,以确保全局唯一
      mapping: {             # 字段映射
         nm: {
            type: "keyword"  # 关键字映射, 与text类似,但不分词
         },
         age: {
            type: "integer"  # 整形映射
         },
         ct: {
            type: "date"     # 日期映射, 实际传值可以是long/Date/String对象
         },
         location: {
            type: "geo_point" # 位置信息, 经纬度, 例如 130.0,23.0代表东经130.0,北纬23.0
         },
         lm: {
            type: "date"     # 日期映射
         },
         data: {             # 这是特殊的字段,代表文件数据
            type: "text",    # 与其他字段一样,text支持分词
            analyzer: "ik_smart",  # 分词模式, ik_smart或者ik_max_word,前者得到的分词数量少,但语义更明确, 后者是尽可能地多分词
            search_analyzer: "ik_smart" # 与分词模式类似,似乎已弃用.
         }
      }
    }
}
```

字段映射类型可查询: https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html

对应的Java类是: org.nutz.walnut.ext.esi.EsiMappingField

用法
=======

```
esi root_init     # 初始化必要的钩子文件,仅root用户执行一次
esi add           # 添加一条记录
esi query         # 查询
esi drop          # 删除整个索引
ftp mapping       # 更新索引配置
```

### 示例

1. 下载并启动es https://gitee.com/nutz/nutzboot/attach_files
2. 修改web_local.properties, 添加 esi.enable=true, 启用esi
3. 登录walnut,进入console
4. 在root账号下执行 esi root_init
5. 进入用户账号, 继续下列操作

```
# 建立数据目录, 名字可以任意
mkdir esi_test
cd esi_test
# 添加索引配置项
obj -u 'esi_conf:{name:"esi_test3", mapping:{nm:{type:"keyword"}, age:{type:"integer"}, ct:{type:"date"}, lm:{type:"date"}, data:{type:"text", "analyzer":"ik_smart", "search_analyzer":"ik_smart"}}}' .

# 创建几条数据
touch wendal;
touch zozoh;
touch pangwu;
touch wizzer;

# 设置一下数据, 或者用wedit来修改
echo "广州" > wendal;
echo "北京" > zozoh;
echo "济南" > pangwu;
echo "中国" > wizzer;

# 开始查询
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
