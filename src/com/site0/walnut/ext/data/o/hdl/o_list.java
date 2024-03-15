package com.site0.walnut.ext.data.o.hdl;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_list extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(hidden)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        String[] paths = params.vals;
        String[] names = Ws.splitIgnoreBlank(params.getString("as"));
        boolean hidden = params.is("hidden");

        // 得到路径列表
        List<String> pathList = new LinkedList<>();
        for (String path : paths) {
            String[] ss = Ws.splitIgnoreBlank(path);
            for (String s : ss)
                pathList.add(s);
        }
        paths = new String[pathList.size()];
        pathList.toArray(paths);

        // 准备查询条件
        NutMap ma = params.getMap("match");
        if (null != ma && ma.isEmpty()) {
            ma = null;
        }

        // 准备排序
        NutMap sort = params.getMap("sort");
        if (null == sort || sort.isEmpty()) {
            sort = Wlang.map("ct", -1);
        }

        // 循环处理
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];

            // 准备名称
            String name = getNameOfPath(path, i, names);

            // 循环处理上下文
            for (WnObj o : fc.list) {
                // 得到目录对象
                WnObj oDir = sys.io.fetch(o, path);
                if (null == oDir || !oDir.isDIR()) {
                    continue;
                }

                // 查询
                WnQuery q = Wn.Q.pid(oDir);
                if (null != ma) {
                    q.setAll(ma);
                }
                q.sort(sort);

                List<WnObj> list = sys.io.query(q);

                // 移除隐藏对象
                if (!hidden) {
                    ListIterator<WnObj> it = list.listIterator();
                    while (it.hasNext()) {
                        if (it.next().isHidden()) {
                            it.remove();
                        }
                    }
                }

                // 记录结果
                o.addv3(name, list);
            }

        }
    }

    private String getNameOfPath(String path, int i, String[] names) {
        if (null == names || names.length == 0) {
            return Files.getMajorName(path);
        }
        if (i >= names.length) {
            return names[names.length - 1];
        }
        return names[i];
    }

}
