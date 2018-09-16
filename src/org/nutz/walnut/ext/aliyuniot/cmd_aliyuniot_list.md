命令简介
======= 

`npower list` 用于查询设备
    

用法
=======

```
npower list [-skip xxx] [-limit xxx]
```

示例
======

```
->: npower list
{
   list: [{
      gmtModified: "Sat, 28-Jul-2018 11:38:02 GMT",
      productKey: "a1eAGqw0Tfi",
      gmtCreate: "Sat, 30-Jun-2018 15:42:52 GMT",
      deviceId: "FrjVEjiojoXjq1C6QwnK",
      deviceName: "869300031699238",
      deviceStatus: "OFFLINE"
   }, {
      gmtModified: "Sat, 28-Jul-2018 13:00:53 GMT",
      productKey: "a1eAGqw0Tfi",
      gmtCreate: "Sat, 28-Jul-2018 13:00:53 GMT",
      deviceId: "b5RweSjzO98qZWGBpkYB",
      deviceName: "869300033624598",
      deviceStatus: "UNACTIVE"
   }],
   pager: {
      pn: 1,
      pgsz: 500,
      pgnb: 1,
      sum: 2,
      skip: 0,
      nb: 2
   }
}
```