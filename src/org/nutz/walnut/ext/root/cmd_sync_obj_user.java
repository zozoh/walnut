package org.nutz.walnut.ext.root;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_sync_obj_user extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        // 只有 root 组管理员才能执行
        int role = sys.usrService.getRoleInGroup(sys.me, "root");
        if (role != Wn.ROLE.ADMIN)
            throw Er.create("e.cmd.nopvg", this.getMyName());

        // 存储一下用户获取的缓存
        final HashMap<String, WnUsr> cacheUsr = new HashMap<String, WnUsr>();
        final HashMap<String, WnObj> cacheGrp = new HashMap<String, WnObj>();

        final WnObj oGrpHome = Wn.checkObj(sys, "/sys/grp");
        final int[] count = new int[1];

        // 处理所有的用户对象
        Stopwatch sw = Stopwatch.begin();

        WnQuery q = new WnQuery();
        int re = sys.io.each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                List<String> keys = new ArrayList<String>(3);
                // 检查 c
                __do_usr(sys, cacheUsr, o, keys, "cid", "c");

                // 检查 m
                __do_usr(sys, cacheUsr, o, keys, "mid", "m");

                // 检查 g
                if (!o.has("gid")) {
                    String str = o.getString("g");
                    WnObj g = cacheGrp.get(str);
                    if (null == g) {
                        g = sys.io.check(oGrpHome, str);
                        cacheGrp.put(str, g);
                    }
                    o.put("gid", g.id());
                    keys.add("gid");
                }

                // 更新
                if (!keys.isEmpty()) {
                    String regex = "^(" + Lang.concat("|", keys) + ")$";
                    sys.io.set(o, regex);
                    count[0]++;
                }

                // 打印
                int n = index + 1;
                if (n % 100 == 0) {
                    sys.out.printlnf("%d-%d) updated %d items :... %s",
                                     n - 100,
                                     n,
                                     count[0],
                                     o.path());
                    count[0] = 0;
                }
            }

            private void __do_usr(WnSystem sys,
                                  HashMap<String, WnUsr> cache,
                                  WnObj o,
                                  List<String> keys,
                                  String key,
                                  String referKey) {
                if (!o.has(key)) {
                    String str = o.getString(referKey);
                    WnUsr u = cache.get(str);
                    if (null == u) {
                        u = sys.usrService.check(str);
                        cache.put(str, u);
                    }
                    o.put(key, u.id());
                    keys.add(key);
                }
            }
        });

        // 打印计数
        sw.stop();
        sys.out.println("--------------------------------------------");
        sys.out.printlnf("All done: %s items walked", re);
        sys.out.println(sw.toString());

    }

}
