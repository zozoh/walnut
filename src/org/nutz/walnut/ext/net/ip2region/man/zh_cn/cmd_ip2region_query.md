# 命令简介 

    `ip2region query` 用来查询ip地址所对应的地区信息

用法
=======


```
ip2region ip2region
     [self]           # 查询当前访问者ip的信息
     [IP1, IP2, IP3]  # 查询给定ip的信息
```

示例
=======

查询当前访问者的ip信息

```
root:~# ip2region query self
{
   "219.136.76.118": {
      region: "中国|华南|广东省|广州市|电信",
      country: "中国",
      zone: "华南",
      province: "广东省",
      city: "广州市",
      isp: "电信"
   }
}
```

查询多个ip的信息,可以与self一起使用

```
root:~# ip2region query self 125.35.205.14
{
   "219.136.76.118": {
      region: "中国|华南|广东省|广州市|电信",
      country: "中国",
      zone: "华南",
      province: "广东省",
      city: "广州市",
      isp: "电信"
   },
   "125.35.205.14": {
      region: "中国|华北|天津市|天津市|联通",
      country: "中国",
      zone: "华北",
      province: "天津市",
      city: "天津市",
      isp: "联通"
   }
}
```