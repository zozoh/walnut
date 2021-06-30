命令简介
======= 

`btea` 使用btea加密/解密数据

用法
=======

```
btea <-k 123456> <-k64 base64key> <-khex hexkey> [-d]
```

参数:
k    - 字符串密钥
k64  - base64编码的密码
khex - hex编码的密码
d    - 解密模式,默认是加密.

btea总是从标准输入读取数据, 然后从标准输出写出结果


示例
=======

```bash
# 加密字符串 123 , 密钥 123456 
echo 123 | btea -k 123456 | base64
>> MTIzCg==
# 将密文进行解密
echo "MTIzCg==" | base64 -d | btea -k 123456 -d
>> 123
```
