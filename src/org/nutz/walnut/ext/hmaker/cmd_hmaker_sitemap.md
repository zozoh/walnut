命令简介
======= 

`hmaker sitemap` 生成SEO所需要的网站地图
    
用法
=======

```    
hmaker sitemap 
        ~/sites/惠人官网                                  # 目标路径，可以是 www目录或者site的工程目录
                                  # 如果是site工程目录，dd则自动根据hm_target_release查找www目录
        [-host www.hurom.com.cn]  # 如果目录没有www属,则dd需要输入,否则自动选第一个
        [-xml]                    # 输出XML格式sitemap   dd
        [-json]                   # 输出Json格式sitemap  dd
        [-txt]                    # 输出txt格式sitemap   dd
        [-write]                  # 与xml/json/txt配合使dd用,将结果直接写入到发布目录
        [-process TMPL]           # 进度模式，只有当 -write 模式，才有效
                                  # 表示不要输出创建的数据详情，而是输出创建的进度
                                  # 参数值是一个模板，占位符用 ${xxx} 来表示
                                  # 占位符会用搜索的网页对象作为上下文，唯一特殊的是
                                  # ${P} 这个是上传比例字符串，格式为 %[10/982]
                                  # 由导入命令来填充
```

调用本命令之前,网站必须先发布好!!!

xml,json,txt可同时生成,配合write参数直接写入网站发布目录

示例
=======

```
# 根据网站的发布内容,直接生成xml/json/txt格式的sitemap并写入文件
demo@~$ hmaker sitemap -host www.leshaonian.com -xml -json -txt -write ~/sites/乐少年官网

# 仅输出txt格式到控制台
demo@~$ hmaker sitemap -host www.leshaonian.com -txt ~/sites/乐少年官网
```