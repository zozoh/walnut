# 过滤器简介

`@modbus16` 计算输入内容的 Modbus CRC16 校验值。

输入来源优先级：管道 > `-f` 文件 > 参数字符串；都没有时按空内容计算。

# 用法

```bash
crc @modbus16 [-hex] [-f 文件] [字符串]
  -hex    # 以十六进制字符串输出（如 C40C），默认输出十进制整数
  -f      # 内容来自指定的文件
```

# 示例

```bash
# 校验字符串（输出十进制）
crc @modbus16 '010300000002'

# 十六进制输出
crc @modbus16 '010300000002' -hex

# 校验文件
crc @modbus16 -f ~/data.bin

# 从管道读入
cat ~/data.bin | crc @modbus16
```
