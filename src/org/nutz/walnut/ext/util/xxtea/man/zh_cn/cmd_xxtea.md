命令简介
======= 

`xxtea` 使用xxtea加密/解密数据

用法
=======

```
xxtea <-k 123456> <-k64 base64key> [-d]
```

参数:
k - 字符串密钥
k64 - base64编码的密码
d - 解密模式,默认是加密.

xxtea总是从标准输入读取数据, 然后从标准输出写出结果


示例
=======

```bash
# 解密字符串 123 , 密钥 123456 
echo 123 | xxtea -k 123456 | base64
>> oEo0/VETxYw=
# 将密文进行解密
echo "oEo0/VETxYw=" | base64 -d | xxtea -k 123456 -d
>> 123
```
