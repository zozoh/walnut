# 命令简介 

    `backup` 备份与还原命令

# 用法

    backup [dump|restore|info]
	
# 示例

```
backup dump    备份指定文件夹
backup restore 根据备份文件还原
backup info    检查备份文件
```

配置文件默认存在在.dump目录下, 备份用户目录时,也会自动跳过该目录