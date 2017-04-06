package org.nutz.walnut.impl.box.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_usermod extends JvmExecutor {

    @Override
    public void exec(final WnSystem sys, final String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "v");
        String userName = null;
        if (params.vals.length == 0) {
            userName = sys.me.name();
        } else {
            if ("root".equals(sys.me.name()) || sys.me.name().equals(params.vals[0])) {
                userName = params.vals[0];
            } else {
                sys.err.println("can't change other people");
                return;
            }
        }

        WnUsr usr = sys.usrService.fetch(userName);
        if (!Strings.isBlank(params.get("G"))) {
            List<String> groups = new ArrayList<>(Arrays.asList(params.get("G").split(",")));

            WnObj grpDir = sys.io.check(null, "/sys/grp");
            List<String> prevGrps = new ArrayList<>();
            sys.io.each(Wn.Q.pid(grpDir.id()), (index, child, length) -> {
                WnObj p = sys.io.fetch(child, "people/" + usr.id());
                if (p != null && p.getInt("role", 0) == 1)
                    prevGrps.add(child.name());
            });
            // 确保用户不会不会被踢出自己的组
            if (!groups.contains(usr.name()))
                groups.add(usr.name());

            // 看看新增啥
            for (String group : groups) {
                if (prevGrps.contains(group)) {
                    prevGrps.remove(group);
                    continue;
                }
                if (!group.matches("[a-zA-Z0-9_]+"))
                    continue;
                sys.exec("touch /sys/grp/" + group + "/people/" + usr.id());
                sys.exec("obj -u 'role:1' /sys/grp/" + group + "/people/" + usr.id());
                sys.out.println("add to group      : " + group);
            }
            // 再看看删除啥
            for (String group : prevGrps) {
                sys.exec("rm /sys/grp/" + group + "/people/" + usr.id());
                sys.out.println("remove from group : " + group);
            }
            return;
        }
        final String path = "/sys/usr/" + usr.name();
        WnObj oUsr = sys.io.check(null, path);
        NutMap meta;
        if (!Strings.isBlank(params.get("E"))) {
            // Json.fromJson(NutMap.class, params.get("E"));
            meta = Lang.map(params.get("E")); // 检查语法
        } else {
            meta = new NutMap();
        }
        String openAppName = params.get("s");
        if (!Strings.isBlank(openAppName)) {
            meta.put("OPEN", openAppName);
        }
        if (!meta.isEmpty()) {
            sys.io.appendMeta(oUsr, meta);
        }
    }

}
