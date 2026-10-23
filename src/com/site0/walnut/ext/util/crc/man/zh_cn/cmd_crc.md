# 命令简介

`crc` 循环冗余校验

# 用法

```bash
crc @modbus16 [-f 文件] [字符串] [-hex]
```

支持的处理器：

```bash
@modbus16    # Modbus CRC16 校验
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
