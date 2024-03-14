package com.site0.walnut.api.io;

/**
 * 声明了几个递归树的模式
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public enum WalkMode {

    /**
     * 深度优先，且首先回调叶子节点
     */
    DEPTH_LEAF_FIRST,

    /**
     * 深度优先，且首先回调中间节点
     */
    DEPTH_NODE_FIRST,

    /**
     * 广度优先
     */
    BREADTH_FIRST,

    /**
     * 只递归叶子节点
     */
    LEAF_ONLY

}
