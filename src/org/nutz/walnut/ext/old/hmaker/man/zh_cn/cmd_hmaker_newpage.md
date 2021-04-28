命令简介
======= 

`hmaker newpage` 创建新页面
    

用法
=======

```    
hmaker /path/to/site newpage [网页名称]
```

 * 默认的，如果不给定网页名称，则认为是 "NewPage"。
 * 如果已经存在了这个网页，则试图采用 NewPage(1) 依次类推，直到可以生成这个网页
     - 如果一直执行 "hmaker newpage" 则会连续 NewPage(1) NewPage(2) 等网页
 * 页面对象的类型均为 "html"，无论你设置什么样的后缀

