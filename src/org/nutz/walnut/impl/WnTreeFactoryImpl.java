package org.nutz.walnut.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnNode;
import org.nutz.walnut.api.io.WnTree;
import org.nutz.walnut.api.io.WnTreeFactory;
import org.nutz.walnut.impl.local.LocalWnTree;

public class WnTreeFactoryImpl implements WnTreeFactory {

    private Map<String, WnTree> cache;

    public WnTreeFactoryImpl() {
        cache = new HashMap<String, WnTree>();
    }

    @Override
    public WnTree check(WnNode nd) {
        WnTree tree = nd.tree();
        // 如果是树的根节点
        if (null != tree && tree.isRootNode(nd)) {
            return tree;
        }

        // 如果是 mount
        String mnt = nd.mount();

        if (!Strings.isBlank(mnt)) {
            WnTree t2 = cache.get(mnt);
            if (null == t2) {
                synchronized (this) {
                    t2 = cache.get(mnt);
                    if (null == t2) {
                        // 本地文件
                        if (mnt.startsWith("file://")) {
                            t2 = new LocalWnTree(this, nd);
                            cache.put(mnt, t2);
                        }
                        // MongoDB
                        else if (mnt.startsWith("mongo:")) {
                            throw Lang.noImplement();
                        }
                        // 不支持的挂载类型
                        else {
                            throw Er.create("e.io.tree.unsupport.mnt", mnt);
                        }
                    }
                }
            }
            return t2;

        }

        // 否则返回节点所在树自身
        return tree;
    }

}
