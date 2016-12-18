package org.nutz.walnut.impl.io.mnt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public static final String MEMORY_TREE = "_memory_tree";
    
    public WnMemoryTree(WnObj obj) {
        this.root = Nodes.create(obj);
    }

    @SuppressWarnings("unchecked")
    public static WnMemoryTree tree(WnObj wobj) {
        Map<String, WnMemoryTree> maptree = Wn.WC().getAs(MEMORY_TREE, Map.class);
        if (maptree == null) {
            maptree = new ConcurrentHashMap<>();
            Wn.WC().put(MEMORY_TREE, maptree);
        }
        String treeId = treeid(wobj);
        WnMemoryTree tree = maptree.get(treeId);
        if (tree == null) {
            tree = newtree(wobj.id());
            maptree.put(treeId, tree);
        }
        return tree;
    }
    
    @SuppressWarnings("unchecked")
    public static WnMemoryTree tree(String treeId) {
        Map<String, WnMemoryTree> maptree = Wn.WC().getAs(MEMORY_TREE, Map.class);
        if (maptree == null) {
            maptree = new ConcurrentHashMap<>();
            Wn.WC().put(MEMORY_TREE, maptree);
        }
        WnMemoryTree tree = maptree.get(treeId);
        if (tree == null) {
            tree = newtree(treeId);
            maptree.put(treeId, tree);
        }
        return tree;
    }
    
    protected static WnMemoryTree newtree(String id) {
        if (log.isDebugEnabled())
            log.debug("new memory tree");
        WnObj obj = new WnBean();
        obj.put("id", MEMORY_TREE + id);
        obj.name(obj.id());
        obj.type("DIR");
        return new WnMemoryTree(obj);
    }
    
    protected static String treeid(WnObj wobj) {
        return wobj.getString("mount_root_id", wobj.id());
    }
}
