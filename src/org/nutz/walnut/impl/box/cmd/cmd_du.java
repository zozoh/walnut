package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_du extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "h");
        String path = params.vals.length > 0 ? params.val(0) : "./";
        path = Wn.normalizePath(path, sys);
        WnObj root = sys.io.check(null, path);
        if (!root.isDIR()) {
            sys.err.println("not dir --> " + path);
            return;
        }
        int depth = params.getInt("d", -1);
        List<WnObj> list = each(sys, root, depth);
        for (WnObj obj : list) {
            String p = obj.path();
            int count = obj.getInt("count");
            sys.out.printlnf("%-40s %s", p, count);
        }
    }

    public List<WnObj> each(WnSystem sys, WnObj rootDir, int depth) {
        List<WnObj> list = new ArrayList<>();
        WnQuery query = Wn.Q.pid(rootDir.id());
        int count = 0;
        list.add(rootDir);
        List<WnObj> objs = sys.io.query(query);
        for (WnObj child : objs) {
            if (child.isDIR()) {
                List<WnObj> _list = each(sys, child, depth);
                for (WnObj clildmap : _list) {
                    count += clildmap.getInt("count");
                    list.add(clildmap);
                }
            } else if (child.isFILE()) {
                count += child.len();
            }
        }
        rootDir.put("count", count);
        return list;
    }
}
