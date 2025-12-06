# 命令简介 

`xo` 用来处理第三方平台的对象存储

# 用法

```bash
xo mkdir        # 写入目录（空对象）
xo write        # 写入对象
xo read         # 读取对象内容
xo list         # 列出对象
xo get          # 查看对象元数据
xo update       # 修改对象元数据
xo rename       # 重命名对象
xo move         # 移动对象到新的路径
xo delete       # 删除对象
xo copy         # 复制对象
xo clear        # 清理对象
```

# 示例

```bash
# 列出当前桶所有对象
xo s3:file @list 
```