package com.site0.walnut.ext.util.react.bean;

public enum ReactType {

    /**
     * 创建数据集对象
     */
    thing_create,

    /**
     * 更新数据集对象
     */
    thing_update,

    /**
     * 删除数据集对象
     */
    thing_delete,

    /**
     * 删除一组符合条件的数据集对象
     */
    thing_clear,

    /**
     * 创建对象
     */
    obj_create,

    /**
     * 更新对象
     */
    obj_update,

    /**
     * 删除对象
     */
    obj_delete,

    /**
     * 删除一组符合条件的对象
     */
    obj_clear,

    /**
     * 执行一段命令脚本模板
     */
    exec,

    /**
     * 执行一段 JS 脚本
     */
    jsc

}
