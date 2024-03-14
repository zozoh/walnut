# 过滤器简介

`@mutate` 操作 DOM 节点，根据配置修改节点

# 用法

```bash
dom @mutate
  [DOM Mutation...]   # 多个对节点的操作
```

## 操作节点的配置对象

```js
// 这是一个系列递归的操作对象
[{
  // 节点选择器，现在仅支持:
  //  - 标签名选择器
  //  - 属性选择器
  //  - 类选择器
  //  - ^... 的为正则表达式形式的标签选择器
  // 多个类选择器，作为并列关系
  selector : [".demo"],
  // 针对节点的操作，可以进行多个操作
  // 可以并行支持多个操作，操作的顺序是：
  //  1. tagName
  //  2. className
  //  3. addClass
  //  4. attrs
  //  5. updateAttrs
  //  6. html
  //  7. text
  //  8. wrap
  //  9. unwrap
  mutation : {
    // 将当前节点改成新的名称
    tagName : "NEW TAG NAME",
    // 重置当前节点的类选择器
    className : "XXXXXX",
    // 增加类选择器
    addClass : "xxxx",
    // 重置节点所有的属性，所有的属性名称，都会变成 kebabCase
    attrs : {
      ...
    },
    // 更新指定的属性，所有的属性名称，都会变成 kebabCase
    updateAttrs : {
      ...
    },
    // 将节点替换为新节点，替换后的新节点将不参与递归
    html : "<HTML ...>",
    // 将节点内容替换为新的文字内容，替换后的节点将不参与递归
    text : "xxx",
    // 将当前节点拆个包
    unwrap : true,
    // 将当前节点用指定节点包裹
    wrap : "NEW TAG"
  },
  // 在选择的节点列表内，递归进行操作
  children : [{/*嵌套操作对象*/}]
}]
```

# 示例

```bash
# 对 DOM 节点进行操作
dom @html a.html @mutate [..]
```

