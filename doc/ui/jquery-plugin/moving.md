# 需要考虑的问题

- 内部窗口拖动，能不能在外部窗口用遮罩捕获时间
- 截获并模拟双击事件
- 辅助线用 canvas 来绘制效率如何
- 放置到拖拽目标里，应该用交集面积大于自己面积的一半(可配置)来决定
- 配置对象给出生成拖拽目标的矩形数据
    + 必须相对于全局 window
    + 包括 jQuery 对象，以及一个 Rect
    + 如果给出的是一个 jQuery，则会自动计算 Rect
- 预处理鼠标位置方式
    + 自由
    + 仅横轴
    + 仅纵轴
    + 固定区域
    + 网格
    + 磁力网格
- 移动回调函数，可以决定 mask.trigger 是否跟着移动 
- 如果没有 findTarget，那么 this 就是 target

# 遮罩层的逻辑

```
window
    iframe
        document.body
            某拖拽目标
    mask
        viewport
        trigger
        canvas
        drops
            dropitem ..
        sensors
            N,S,W,E ...
        
```

