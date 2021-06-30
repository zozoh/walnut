命令简介
======= 

`iotpower activate` 激活或初始化命令

用法
=======

```
iotpower activate
```

参数通过标准输入读取,格式为json

```json
{
   devid: "abc...def", // [必]设备的硬件识别号,必填
   rint : 0x13455233 // [选]随机数,如果是初始化设备,不需要填, 如果是生成激活码,必填
   devsecret: "xxxxxxx"  // [选]设备当前的加密devid,长度为32字节的hex字符串(对应二进制的16字节数据),获取激活码时必选
}
```

示例
=======

```bash
// 初始化
echo '{devid:"0072002B0947303032333230"}' | iotpower activate
>> // 以下为输出
{
   "devsecret": "205305E72966360C121233F120210602", // 根据devid和rint算出的新的devid16
}
```

```bash
// 激活, 随机数的默认值是0x20210629,设备激活时必须选新的值
echo '{rint:0x20210629,devid:"0072002B0947303032333230",devsecret:"205305D929663613121233F820210609"}' | iotpower activate
>> // 以下为输出
{
   "devsecret": "205305E72966360C121233F120210602", // 根据devid和rint算出的新的devid16
   "devcode": "2021237C20210622" // 激活码,下发给上位机
}
```
