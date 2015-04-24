package org.nutz.walnut.api.io;

import org.nutz.lang.Each;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.util.UnitTestable;

public interface WnTree extends UnitTestable {

    WnTreeFactory factory();

    WnNode getTreeNode();

    void setTreeNode(WnNode treeNode);

    int eachMountTree(Each<WnTree> callback);

    boolean isTreeNode(String id);

    WnNode getNode(String id);

    int eachChildren(WnNode p, String str, Each<WnNode> each);

    boolean hasChildren(WnNode nd);

    void walk(WnNode p, Callback<WnNode> callback, WalkMode mode);

    WnNode fetch(WnNode p, String path);

    boolean exists(WnNode p, String name);

    /**
     * 获取一个子节点
     * 
     * @param p
     *            父节点
     * @param paths
     *            路径数组（0 base）
     * @param fromIndex
     *            数组开始下标（包含）
     * @param toIndex
     *            数组结束下标（不包含）
     * @param callback
     *            每次进入一个节点的回调
     * @return 节点
     */
    WnNode fetch(WnNode p, String[] paths, int fromIndex, int toIndex);

    WnNode create(WnNode p, String path, WnRace race);

    WnNode create(WnNode p, String[] paths, int fromIndex, int toIndex, WnRace race);

    WnNode createNode(WnNode p, String id, String name, WnRace race);

    void delete(WnNode nd);

    void rename(WnNode nd, String newName);

    WnNode append(WnNode p, WnNode nd);

    void setMount(WnNode nd, String mnt);

    boolean equals(Object obj);

}
