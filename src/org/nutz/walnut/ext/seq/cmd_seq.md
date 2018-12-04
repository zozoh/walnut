# 命令简介 

 `seq` 序列生成器

## 用法

    seq [$cmd] [$args]

## 支持的命令

```
seq next        # 根据条件生成序列的下一个值
seq create      # 为一组对象生成序列值
```

## 序列生成规则

1. 输入所需要的额外变量
2. 输入起始值,可选
3. 输入模板字符串
4. 输出序列值
5. 更新对象字段

可用的变量:
1. seq  当前序列值,是一个自增的值,可设置开始值,每步增量
2. wobj 当前WnObj对象,可选
3. 其他自定义变量

默认使用Tmpl的语法, 也许会增加更多语法支持

### 例子A, 单纯数值序列, 简单+1

```
${seq}   = 1
${seq}   = 2
${seq}   = 3
${seq}   = 4
```

### 例子B, 固定长度3的数值序列

```
${seq<int:%03d>} = 001
${seq<int:%03d>} = 002
${seq<int:%03d>} = 003
${seq<int:%03d>} = 004
```

### 例子C, 含当前对象某个字段的值

```
${seq<int:%03d>}${wobj.u_sex} = 001M // 当前WnObj的u_sex属性的值是M
${seq<int:%03d>}${wobj.u_sex} = 002F // 当前WnObj的u_sex属性的值是F
${seq<int:%03d>}${wobj.u_sex} = 003F
${seq<int:%03d>}${wobj.u_sex} = 004M
```

### 例子D, 含参考对象的某个字段的值,也含当前对象某个字段的值,还有额外的字符串

```
${prefix}${seq<int:%03d>}${wobj.u_sex} = A001M // 当前WnObj的u_sex属性的值是M,prefix的值为A
${prefix}${seq<int:%03d>}${wobj.u_sex} = B002F // 当前WnObj的u_sex属性的值是F,prefix的值为B
${prefix}${seq<int:%03d>}${wobj.u_sex} = A003F
${prefix}${seq<int:%03d>}${wobj.u_sex} = B004M
```

