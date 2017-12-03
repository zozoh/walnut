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
   "ipcname": "文尔平台",
   "ipchost": "site0.cn",
   "ipcno": "粤ICP备14009452号-5",
   "ipcdate": "2017-12-03"
}
root:~#
```