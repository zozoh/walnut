命令简介
======= 

`hmaker sitemap` 生成SEO所需要的网站地图
    
用法
=======

```    
hmaker ~/sites/惠人官网
        [-host www.hurom.com.cn]      # 如果目录没有www属性,则需要输入,否则自动选第一个
        [-xml]                        # 输出XML格式sitemap
        [-json]                       # 输出Json格式sitemap
        [-txt]                        # 输出txt格式sitemap
        [-write]                      # 与xml/json/txt配合使用,将结果直接写入到发布目录
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