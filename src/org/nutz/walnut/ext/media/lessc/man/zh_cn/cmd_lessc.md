# 命令简介 

    `lessc` 用来编译less为css文本

用法
=======

```
lessc compile  /path/to/less     # 要编译的less文件，所在目录为当前目录
            [-include-path xxx]  # 一组;分隔路径，当@import文件在当前目录没有，从这里找
            [-pri-path xxx]      # 一组;分隔路径，当@import文件优先从这里找
    
```

实例
=======

单文件渲染,无import

```
> echo '.class { width: (1 + 3) }' > simple.less
> lessc compile simple.less

.class {
  width: 4;
}
```

单文件渲染,带import

```
> echo '@import "another.less";.class { width: (1 + 3) }' > with_import.less
> echo 'a { width : (100 - 80)}' > another.less
> lessc compile with_import.less
a {
  width: 20;
}
.class {
  width: 4;
}
```