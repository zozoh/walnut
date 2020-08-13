# 命令简介 

    `mount` 命令将扩展媒介的内容挂载到

# 用法

    mount [媒介] [挂载点]
    
# 示例

    // 将一个本地磁盘目录挂载到一个已存在的目录上
    mount file://~/worksapce/data/abc ~/abc
    
    // 将一个七牛云存储的库挂载到一个已存在的目录上
    // 该目录必须有这些属性: 
    //    qiniu_ak 密钥AK
    //    qiniu_sk 密钥SK
    //    qiniu_bucket 仓库名
    //    qiniu_domain 七牛分配的域名或自定义域名,需要http://或https://前缀
    
    mount qiniu://walnut-wendal
    
    // 列出所有挂载点
    
    mount
    
    // 重新挂载配置文件中的挂载点
    
    mount -init
    
