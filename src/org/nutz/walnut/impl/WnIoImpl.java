package org.nutz.walnut.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnStore;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.impl.security.WnSecurity;
import org.nutz.walnut.util.Wn;

public class WnIoImpl extends AbstractWnIo {

    @Override
    public WnObj get(String id) {
        // 从树里查找节点
        WnNode nd = tree.getNode(id);
        if (null == nd)
            return null;

        // 读取所有父节点
        List<WnNode> parents = new LinkedList<WnNode>();
        tree.loadParents(nd, true, parents);

        // TODO 针对所有的树节点，检查权限
        // ..

        // 得到索引
        WnObj o = indexer.get(id);

        // 没有索引，生成一个对象
        if (null == o) {
            o = new WnBean();
        }

        // 用节点的数据覆盖对象对应的字段并返回
        return o.setNode(nd);
    }

    @Override
    public WnObj fetch(WnObj p, String path) {
        WnSecurity secu = security(Wn.Io.R);
        tree.fetch(p, path, secu);
        return secu.getLastObj();
    }

    @Override
    public WnObj fetch(WnObj p, String[] paths, int fromIndex, int toIndex) {
        WnSecurity secu = security(Wn.Io.R);
        tree.fetch(p, paths, fromIndex, toIndex, secu);
        return secu.getLastObj();
    }

    @Override
    public void walk(WnObj p, Callback<WnObj> callback, WalkMode mode) {
        final WnSecurity secu = security(Wn.Io.R).callback(callback);
        tree.walk(p, secu, mode);
    }

    @Override
    public WnObj move(WnObj o, String destPath) {
        // 不用移动
        String path = o.path();
        if (path.equals(destPath))
            return o;

        // 得到自身节点
        WnNode ndA = tree.getNode(o.id());
        if (null == ndA) {
            throw Er.create("e.io.noexists", o);
        }

        // 得到安全检查器
        WnSecurity secu = security(Wn.Io.RW);

        // 看看目标是否存在
        WnNode ta = tree.fetch(null, destPath, secu);

        // 如果不存在
        if (null == ta) {
            ta = tree.fetch(null, Files.getParent(destPath), secu);
        }

        // 还不存在不能忍啊
        if (null == ta) {
            throw Er.create("e.io.noexists", destPath);
        }

        // 默认返回自身
        WnObj re = o;

        // 准备好了父在以后判断是否是同一颗树
        WnTree treeA = ndA.tree();
        WnTree treeB = ta.tree();
        // 如果在同一颗树上则简单修改一下节点
        if (treeA.equals(treeB)) {
            // 否则只能允许把对象移动到对象下面
            if (ta.isDIR() || (ta.isOBJ() && ndA.isOBJ())) {
                treeA.append(ta, ndA);
            }
            // 这肯定是一个非法的移动
            else {
                throw Er.create("e.io.move.forbidden", ndA.path() + " >> " + destPath);
            }
        }
        // 如果不是同一棵树，则一棵树上创建一颗树上删除
        else {
            WnNode ndB = treeB.createNode(ta, ndA.id(), ndA.name(), ndA.race());
            // 保持 mount
            if (ndA.isMount()) {
                treeB.setMount(ndB, ndA.mount());
            }
            // copy 内容
            WnObj oB = new WnBean().setNode(ndB);
            WnStore storeA = stores.get(ndA);
            WnStore storeB = stores.get(ndB);

            InputStream ins = storeA.getInputStream(o, 0);
            OutputStream ops = storeB.getOutputStream(oB, 0);
            Streams.writeAndClose(ops, ins);

            // 删除原来的内容
            storeA.cleanHistory(o, -1);

            // 删除旧节点
            treeA.delete(ndA);

            // 那么返回的就是 oB
            re = oB;
        }

        // 更新一下索引的记录
        indexer.setValue(o.id(), "pid", ta.id());

        return re;
    }

    @Override
    public WnObj create(WnObj p, String path, WnRace race) {
        WnSecurity secu = security(Wn.Io.RW);
        tree.create(p, path, race, secu);
        return secu.getLastObj();
    }

    @Override
    public WnObj create(WnObj p, String[] paths, int fromIndex, int toIndex, WnRace race) {
        WnSecurity secu = security(Wn.Io.RW);
        tree.create(p, paths, fromIndex, toIndex, race, secu);
        return secu.getLastObj();
    }

    @Override
    public void delete(WnObj o) {
        WnSecurity secu = security(Wn.Io.RW);
        tree.delete(o);
    }

    @Override
    public void remove(String id) {
        WnObj o = this.get(id);
        delete(o);
    }

}
