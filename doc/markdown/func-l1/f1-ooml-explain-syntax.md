---
title: OOML 模板文件语法
author: zozoh
---

# OOML模板文件语法

占位符分作两种：

1. 字面量占位符： 可以在 word 文档编辑器里直接通过 `${xxx}` 形式书写
2. 图片占位符: 在 word 文档插入一张图片，并修改图片的【可选文字】作为标记

## 字面量占位符

- 普通占位符 : `${变量名称}`
- 选择框占位符 : `${变量名称#checkbox==true?false}`
   - `#checkbox` 声明了这个占位符的类型（大小写不敏感）
   - `==true` 表示变量为何值时才为**真**
   - `?false` 表示默认的值。即变量集合中未定义或者 `null` 值时，采用的值
- 段循环开始占位符 : `${变量名#loop-begin:it}`
   - 仅仅对 `w:tr` 和 `w:p` 有效, 可以循环表格行或者普通段落行
   - `变量名` 需要对应上下文中的数组或者列表型变量
   - `#loop-begin` 声明了这个占位符的类型（大小写不敏感）
   - `:it` 循环内，子变量名称，作用域仅在循环内
   - 一直到遇到循环结束占位符，之间的段落，将会被循环输出
   - 段落循环占位符的**开始**和**结束**必须有同一个父节点
- 段循环结束占位符 : `${#loop-end}`
   - 根据变量名，结束之前对应的段落循环占位符
   - 循环不支持嵌套，以便防止愚蠢的无穷递归
- 超链接占位符 : `${变量名称#age==18?√}` □ddd 
   - 声明在超链接中
   - `#checkbox` 声明了这个占位符的类型（大小写不敏感）
   - `==true` 表示变量为何值时才为**真**
   - `?false` 表示默认的值。即变量集合中未定义或者 `null` 值时，采用的值

## 图片占位符

在文档中随便插入一张图片，右键： 【设置自选图形/图片格式 > 可选文字】，输入 `=变量名`。
在上下文中，这个变量名通常对应一个系统中的文件路径。

在 OOML 中，这个图片的源码通常为：

```xml
<v:shape 
   id="_x0000_s1029" 
   type="#_x0000_t75" 
   alt="=嵌入图片" 
   style="position:absolute;left:0;text-align:left;margin-left:9.55pt;margin-top:-241.65pt;width:275.2pt;height:245.15pt;z-index:1;mso-position-horizontal-relative:text;mso-position-vertical-relative:text">
      <v:imagedata r:id="rId6" o:title="图片的说明"/>
      <w10:wrap type="topAndBottom"/>
</v:shape>
```

通过判断 `<v:shape>` 的 `alt` 属性，是否以 `=` 开始，就能决定这个图片是否为动态的。
如果是动态图片，会执行下面的展开逻辑：

```bash
1. 找到子节点 `<v:imagedata>` 得到 `r:id` 属性
2. 根据 rId 在 document.xml.rels 文件中找到 <Relationship>，并得到对应的图片路径 media/image1.png
3. 找到这个图片所在的条目
3.1 如果图片的扩展名与嵌入图片一致，直接向其写入嵌入图片
3.2 否则写入嵌入图片后，将其后缀名进行修改
3.3 并且，需要确保 [Content_Types].xml 文件中声明了这个扩展名所对应的 MIME 类型
```

# OOML模板展开的逻辑

## Bean定义

- `OomlWRunList` : 分析普通占位符列表
- `OomlWPlaceholder` : 占位符定义
- `OomlWPhMark` : 占位符位置标记
- ``

## Run分析器

目的是记录 `<w:p>` 下面的 `<w:r>` 是怎么形成占位符的

```bash
OomWRunList
  - List<CheapElement> runNodes          # 收集一个<w:r>列表
  - List<OomlWPlaceholder> placeholders  # 准备一个占位符列表
     - String name                       # 占位符名称
     - String boolTest                   # 如果是布尔型，测试的值是什么
     - String defaultValue               # 默认值
     - String itemName                   # 如果是循环占位标记，循环内变量名
     - OomlWPhType type                  # 占位符类型（枚举）
     - CheapElement runProperty          # 属性元素（采用第一个占位符元素）
     - OomlWPhMark runBegin              # 开始
        - index  : 0                     # 列表中元素下标
        - offset : 0                     # 在文本中的偏移量
     - OomlWPhMark   runEnd              # 结束
        - index  : 0                     # 列表中元素下标
        - offset : 0                     # 在文本中的偏移量
```

## 展开逻辑伪代码

```bash
采用深度优先策略，遍历整个文档，依次用下面的逻辑处理节点

节点处理逻辑 ($node, $vars) {
  #------------------------------------------------
  <w:p> 节点
  {
    #
    # 循环段落
    #
    1. 得到节点的纯文本
    2. 判断是否为 `${变量名#loop-begin:it}`
    {
        3. 寻找到 `${变量名#loop-end}` 的 `<w:p>`
           中间的 `<w:p>` 将被收集起来
        4. 达到父节点的末尾或者找到结束节点
        {
          5. 找到变量名对应的上下文列表数据
          {
             6. 设置上下文变量 `it`
             7. 输出收集的中间 `<w:p>`
                > 递归调用 `节点处理逻辑`
             8. 删除上下文变量 `it`
          }
        }
    }
    #
    # 普通占位符
    #
    1. 准备一个 run 分析器实例
    2. 收集一个 <w:r> 的列表
       > 分析器.load (`<w:p>`)
    3. 分析器.prepare() 分析列表
    4. 分析器.explain($vars) 展开列表
  }
  #------------------------------------------------
  <w:tr> 节点
  {
    1. 得到节点的纯文本
    2. 判断是否为 `${变量名#loop-begin:it}`
    {
        3. 寻找到 `${变量名#loop-end}` 的 `<w:tr>`
           中间的 `<w:tr>` 将被收集起来
        4. 达到父节点的末尾或者找到结束节点
        {
          5. 找到变量名对应的上下文列表数据
          {
             6. 设置上下文变量 `it`
             7. 输出收集的中间 `<w:tr>`
                > 递归调用 `节点处理逻辑`
             8. 删除上下文变量 `it`
          }
        }
    }
  }
  #------------------------------------------------
  <v:shape> 节点
  1. 判断是否有属性 alt="=变量名"
  {
     2. 找到子节点 `<v:imagedata>` 得到 `r:id` 属性
     3. 根据 rId 在 document.xml.rels 文件中找到 <Relationship>，并得到对应的图片路径 media/image1.png
     4. 找到这个图片所在的条目 
     {
        5. 直接向其写入嵌入图片
        6. 如果图片的扩展名与嵌入图片不一致
        {
           7. 将其后缀名进行修改
           8. 确保 [Content_Types].xml 文件中声明了这个扩展名所对应的 MIME 类型
        }
     }
  }
  #------------------------------------------------
  其他节点 
  {
    进行递归
  }
  #------------------------------------------------
}
```
