package org.nutz.walnut.impl.io.mnt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.Nodes;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.WnMounter;
import org.nutz.walnut.impl.io.bucket.MemoryBucket;

public class MemoryMounter implements WnMounter {
    
    private static final Log log = Logs.get();
    public static boolean DEBUG = false;
    
    public WnObj get(MimeMap mimes, WnObj mo, String[] paths, int fromIndex, int toIndex) {
        Node<WnObj> p = WnMemoryTree.tree(mo).root;
        OUT: for (int i = fromIndex; i < paths.length && i < toIndex; i++) {
            for (Node<WnObj> node : p.getChildren()) {
                if (paths[i].equals(node.get().name())) {
                    p = node;
                    continue OUT;
                }
            }
            if (DEBUG)
                log.warnf("not such file > /%s : %s", Strings.join("/", paths), paths[i]);
            // 遍历child还是找不到,那就没有咯
            return null;
        }
        return p.get().clone();
    }

    public List<WnObj> getChildren(MimeMap mimes, WnObj mo, String name) {
        List<WnObj> children = new ArrayList<>();
        Pattern pattern = null;
        if (name != null) {
            if (name.equals("*")) {
                name = null;
            } else if (name.endsWith("*")) {
                name = name.substring(0, name.length() - 1);
                pattern = Pattern.compile("^"+name);
            } else {
                pattern = Pattern.compile(name);
            }
        }
        WnMemoryTree tree = WnMemoryTree.tree(mo);
        Node<WnObj> pnode = tree.maps.get(mo.id());
        if (pnode == null)
            pnode = tree.root;
        for (Node<WnObj> node : pnode.getChildren()) {
            if (pattern != null && !pattern.matcher(node.get().name()).find()) {
                continue;
            }
            children.add(node.get().clone());
        }
        return children;
    }

    public void create(WnObj p, WnObj o) {
        WnMemoryTree tree = WnMemoryTree.tree(p);
        Map<String, Node<WnObj>> nodemap = tree.maps;
        Node<WnObj> pnode = nodemap.get(p.id());
        if (pnode == null)
            pnode = tree.root;
        for (Node<WnObj> node : pnode.getChildren()) {
            if (node.get().name().equals(o.name())) {
                throw Er.createf("e.io.obj.exists", "%s/%s", p.path(), o.name());
            }
        }

        o.mount(p.mount());
        
        String mount_root_id = p.getString("mount_root_id");
        if (mount_root_id == null)
            mount_root_id = p.id();
        o.put("mount_root_id", mount_root_id);
        
        String mount_root_path = p.getString("mount_root_path");
        if (mount_root_path == null)
            mount_root_path = p.path();
        o.put("mount_root_path", mount_root_path);

        String id = o.path().substring(mount_root_path.length() + 1);
        o.data("memory://"+R.UU32());
        o.id(mount_root_id+":memory:%%"+id.replace('/', '%'));
        MemoryBucket bucket = new MemoryBucket(64*1024);
        
        tree.datas.put(o.data(), bucket);
        Node<WnObj> node = Nodes.create(o.clone());
        pnode.add(node);
        nodemap.put(o.id(), node);
    }
    
    public void remove(WnObj obj) {
        WnMemoryTree tree = WnMemoryTree.tree(obj);
        
        Node<WnObj> node = tree.maps.get(obj.id());
        if (node != null) {
            if (node.hasChild()) {
                node.getChildren().forEach((n)-> remove(n.get()));
            }
            // 找到父节点,然后删除自己
            node.remove();
        }
        // 从 id -> data 映射中移除
        tree.datas.remove(obj.id());
        // 从 id -> Node 映射中移除
        tree.maps.remove(obj.id());
    }
    
    public void set(String id, NutMap map){
        Node<WnObj> node = WnMemoryTree.tree(id.substring(0, id.indexOf(':'))).maps.get(id);
        if (node != null)
            node.get().putAll(map);
    }
}
