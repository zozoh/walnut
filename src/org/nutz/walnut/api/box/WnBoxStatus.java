package org.nutz.walnut.api.box;

/**
 * 声明了一个沙箱的三种状态
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public enum WnBoxStatus {

    /**
     * 沙箱正在运行某任务
     */
    RUNNING,

    /**
     * 沙箱已经被分配，但是还没有运行任务
     */
    IDLE,

    /**
     * 沙箱没有被分配
     */
    FREE

}
