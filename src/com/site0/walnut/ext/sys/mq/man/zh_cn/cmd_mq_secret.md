命令简介
======= 

`mq secret` 为当前用户生成队列密钥

用法
=======

```bash
mq secret 
  [-len 50]    # 【选】生成多少位密钥，默认 50
  [-Q]         # 【选】静默输出，否则会显示密钥
```

示例
=======

```bash
# 重新生成密钥
demo:> mq secret

# 生成 80 位密钥
demo:> mq secret -len 80
```