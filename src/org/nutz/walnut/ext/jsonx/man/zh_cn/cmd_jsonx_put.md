过滤器简介
======= 

`@put` 向上下文增加特殊的键
 

用法
=======

```bash
# 强制覆盖
echo '{}' | jsonx @put 'pos' '{x:100}'

# 仅仅作为默认值添加
echo '{}' | jsonx @put -dft 'pos' '{x:100}'
```

