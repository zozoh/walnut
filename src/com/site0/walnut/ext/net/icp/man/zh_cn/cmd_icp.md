# 命令简介 

    `icp` 用来查询和管理ICP备案信息

用法
=======

```
icp query           # 根据域名查备案
```

实例
=======

```
root:~# icp query site0.cn
{
   "ltdname": "广州市文尔软件科技有限公司 在营（开业）",
   "icpname": "文尔平台",
   "icphost": "site0.cn",
   "icpno": "粤ICP备14009452号-5",
   "icpdate": "2017-12-03",
   "ip" : "111.230.171.211"
}
root:~#
```