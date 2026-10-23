# 命令简介

`esi` 用来处理与 Elasticsearch 服务器的交互。

本命令依赖元数据 `esi_conf`，声明索引名称和字段映射：

```bash
# 索引配置示例（esi_conf 元数据）
{
    esi_conf : {
        name : "esi_test",    # 索引名称，必须存在
                              # 实际存储的索引名是 <用户>_<name>，保证全局唯一
        mapping: {            # 字段映射
            nm: { type: "keyword" },    # 关键字，不分词
            age: { type: "integer" },   # 整数
            ct: { type: "date" },       # 日期，值可以是 long/Date/String
            location: { type: "geo_point" },  # 经纬度，如 130.0,23.0
            data: {             # 特殊字段：文件正文
                type: "text",
                analyzer: "ik_smart",   # ik_smart 或 ik_max_word
                search_analyzer: "ik_smart"
            }
        }
    }
}
```

字段映射类型参见 [Elasticsearch 文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html)。

# 用法

```bash
esi [配置对象路径|.] @处理器 [参数...]
```

支持的处理器：

```bash
@root_init    # 初始化必要的钩子文件，仅 root 用户执行一次
@mapping      # 更新索引配置，新建或修改 esi_conf 后执行
@query        # 查询
@add          # 添加/更新一条记录（添加和更新不区分）
@delete       # 删除一条记录
@drop         # 删除整个索引
```

一般会使用钩子自动完成数据的增删改查，但仍保留手工维护的命令。

# 示例

```bash
# 1. 启动 ES，并在 web_local.properties 里添加 esi.enable=true
# 2. 在 root 账号下执行一次
esi @root_init

# 3. 进入用户账号，建立数据目录并声明索引配置
mkdir esi_test
cd esi_test
o . @update 'esi_conf:{name:"esi_test3", mapping:{nm:{type:"keyword"}, age:{type:"integer"}, ct:{type:"date"}, data:{type:"text", analyzer:"ik_smart", search_analyzer:"ik_smart"}}}'

# 4. 创建几条数据
echo "广州" > wendal
echo "北京" > zozoh

# 5. 查询（输出格式与 o @query 一致，每个结果多一个 esi_hit_score 表示匹配度）
esi . @query -match 'nm:"*w*"' -l -pager -sort ct:-1
```
