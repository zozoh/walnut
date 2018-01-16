## 命令简介 

    `tfodt` Tensorflow Object Detection API
    
## 基本流程

* train_add 创建新的训练任务
* 编辑训练所需要的label
* 编辑 config
* 指定预训练模型
* 准备素材,放入训练目录
* tfrecord_create 生成tf文件
* 分配训练机(扩展用)
* train_begin 开始/重启训练进程
* train_step_query 查询当前训练step数
* train_checkpoint_list 查询已经生成的checkpoint文件
* train_exportpb 根据checkpoint生成export_inference_graph,用于训练
* train_end 终止训练过程
* train_clear 移除训练任务