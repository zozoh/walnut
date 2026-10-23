# 过滤器简介

`@objsha1` 修改某个系统对象的 SHA1 引用。

# 用法

```bash
refer @objsha1 对象路径 长度 新SHA1 [时间]
       # 对象路径    # 目标对象的路径
       # 长度        # 新内容的字节长度
       # 新SHA1      # 新的 SHA1
       # 时间        # 最后修改时间，不给则自动更新为当前时间
```

# 示例

```bash
# 修改对象的 SHA1 引用
refer @objsha1 ~/abc.txt 1560 54af..9a2e
```
