package org.nutz.walnut.impl.io.mnt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nutz.lang.random.R;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.Nodes;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnBucket;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.Wn;

public class WnMemoryTree {
    
    private static final Log log = Logs.get();
    
    public Node<WnObj> root;
    public Map<String, Node<WnObj>> maps = new ConcurrentHashMap<>();
    public Map<String, WnBucket> datas = new ConcurrentHashMap<>();
    
    public WnMemoryTree(WnObj obj) {
        this.root = Nodes.create(obj);
    }

    public static WnMemoryTree tree() {
        WnMemoryTree tree = Wn.WC().getAs("_memory_tree", WnMemoryTree.class);
        if (tree == null) {
            log.debug("new memory tree ");
            WnObj obj = new WnBean();
            obj.put("id", "_memory_tree_" + R.UU32());
            obj.name(obj.id());
            obj.type("DIR");
            tree = new WnMemoryTree(obj);
            Wn.WC().setv("_memory_tree", tree);
        }
        return tree;
    }
}
