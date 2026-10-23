# 过滤器简介

`@coms` 用来获取当前域所有可被编辑的控件的定义信息。

所谓*可编辑*，即控件目录有 `_hmaker.json` 定义文件的全局控件。它会遍历给定的控件库路径，发现可编辑控件就加到结果里。

# 命令输出结果

```js
// 命令会查看每个控件的 _com.json 和 _hmaker.json 生成如下格式
[{
  path : "ti/list",    // 自动计算出的控件相对路径
  name: "ti-list",     // 控件全库唯一名称（来自 _com.json）
  icon: "im-data",          // 控件图标
  title: "i18n:com-list",   // 控件显示名
  manual   : true,    // 是否有开发手册
  tutorial : true,    // 是否有使用说明
  video    : true,    // 是否有视频教程
  // 适用屏幕：desktop 桌面 | tablet 平板 | phone 手机
  screen: ["desktop", "tablet"]
  editComType : "ti-form"    // 采用的编辑控件
  editComConf : { /*...*/ }  // 编辑控件的配置信息
}]
```

# 用法

```bash
ti @coms [控件库路径] [-f] [-cache 路径] [-cqn]
  # 控件库路径    # 默认为 /rs/ti/com
  -f            # 无视缓存，强行再搜索一遍
  -cache        # 指定缓存文件路径，默认 /etc/ti/coms/<库名>.json
  -cqn          # JSON 输出的格式化开关
```

# 示例

```bash
# 输出默认组件库
ti @coms

# 强制重新搜索默认组件库
ti @coms -f
```
