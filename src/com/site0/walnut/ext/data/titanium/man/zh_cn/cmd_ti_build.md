# 命令简介 

`ti build` 根据配置信息，将控件库或者核心库打包

-------------------------------------------------------------
# 配置文件格式

```js
{
  "entries" : [{
        "path": "src/core/ti.mjs",
        "target": "core",
        "version": "^( *\"version\" *: *\")([0-9.]+)-dev(\" *, *)$"
      }
      }],
  "targets" : {
    "all": {
      "path" : "src/dist/es6/ti-more-all.js",
      "wrap" : true
    }
  }
}
```

-------------------------------------------------------------
# 用法
 
```bash
ti build
  [/path/to]        # 控件库路径（默认为 ti-build.json）
```

-------------------------------------------------------------
# 示例

```bash
# 编译输出
demo:~$ ti build /rs/ti
```