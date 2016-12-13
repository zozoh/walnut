package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.random.R;
import org.nutz.lang.util.Node;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.impl.io.mnt.WnMemoryTree;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class cmd_tempfile extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        if (params.vals.length == 0) {
            WnMemoryTree tree = WnMemoryTree.tree();
            Map<String, Node<WnObj>> maps = tree.maps;
            sys.out.println("count=" + (maps.size() - 1));
            List<NutMap> list = new ArrayList<>(maps.size());
            maps.values().forEach((it)-> list.add((NutMap)it.get()));
            Cmds.output_objs_as_table(sys, params, null, list);
            maps.values().forEach((it)-> {
                if (!it.get().isFILE())
                    return;
                sys.out.printlnf(">> %s size=%s", it.get().id(), tree.datas.get(it.get().data()).getSize());
            });
            return;
        }
        String tmpname = R.UU32() + "." + params.val_check(0);
        WnObj tmp = sys.io.create(null, "/tmp/" + tmpname, WnRace.FILE);
        sys.out.print(tmp.id());
    }

}
