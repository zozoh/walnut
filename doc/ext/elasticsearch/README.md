# 使用elasticsearch扩展walnut的全文索引能力

## 为什么要添加elasticsearch

* 存在大量搜索需求,例如查药名,商品名,而且都是模糊查询
* mongodb的字段索引在上述查询中不生效,导致全表索引
* mongodb 3.4开始支持中文的全文索引,但属于收费项目
* elasticsearch属于专业的搜索引擎方案,能满足上述查询

## 何为elasticsearch

自述:

    Elasticsearch 是一个分布式、RESTful 风格的搜索和数据分析引擎，能够解决不断涌现出的各种用例。

* 分布式: 支持集群,自动分片,手工分片
* RESTful: 使用Http 协议通信, 基于路径参数和Json数据进行交互
* 搜索和数据分析引擎: 支持全文索引,支持聚合操作(map-reduce)

官网: https://www.elastic.co/cn/products/elasticsearch

## 选择何种版本

鉴于将来的扩展性需要, 限定在阿里云支持的2个版本上: https://help.aliyun.com/document_detail/57770.html?spm=a2c4g.11174283.3.1.21037958TLSp0d

分别是: 5.5.3 和 6.3.2

其中6.3.2包含xpack扩展, 且它是6.3.x的最后版本,所以选用它

何为xpack: 开发和维护的功能, https://www.elastic.co/cn/products/stack

## 连接方式

elasticsearch支持多种连接方式:

* JDBC驱动,但这属于收费项目,暂不考虑
* Java X Level REST Client
    * Low Level, 底层通信封装
    * High Level, 将操作封装为增删改查API

鉴于使用的方便性, 选用 Java High Level REST Client

## 配置文件

全局配置位于 /etc/elasticsearch/conf 场景是什么?

个人配置位于 ~/.elasticsearch/myp/conf

配置文件的名字就是数据集的名称,例如myp

```json
{
    pid : "abc...def" # [必]数据集关联的文件夹
    fields : {        # [必]指定哪些字段要索引
        xx   : "auto",   # 自动判断
        name : "string", # 字符类型
        age  : "number", # 数值类型
        chk  : "boolean",# 布尔型
        ct   : "date",   # 日期
        ...
        // id是自动添加的,不需要填
    }
}
```