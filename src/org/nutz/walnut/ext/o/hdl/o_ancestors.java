package org.nutz.walnut.ext.o.hdl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_ancestors extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(self|notop|keep)");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        boolean includeSelf = params.is("self");
        boolean excludeTop = params.is("notop");
        String asKey = params.val(0, null);
        if (null != asKey) {
            // 跳过本操作
            if ("~ignore~".equals(asKey)) {
                return;
            }
            if ("null".equals(asKey)) {
                asKey = null;
            }
        }

        // 存储所有的读取列表
        List<List<WnObj>> res = new ArrayList<>(fc.list.size());

        // 声明了固定的顶
        String phTop = params.getString("until", null);
        WnObj oTop = null;
        if (!Ws.isBlank(phTop)) {
            oTop = Wn.checkObj(sys, phTop);
        }

        // 首先读取祖先节点
        for (WnObj o : fc.list) {
            List<WnObj> ans = new LinkedList<>();
            o.loadParents(ans, false);

            if (ans.isEmpty())
                continue;

            // 截取一下顶
            if (null != oTop) {
                // 尝试找一下顶
                Iterator<WnObj> it = ans.listIterator();
                boolean foundTop = false;
                while (it.hasNext()) {
                    WnObj an = it.next();
                    if (an.isSameId(oTop)) {
                        foundTop = true;
                        break;
                    }
                }

                // 找到了顶，那么就开始复制后面的节点
                // 找不到顶，我看，就全输出吧，否则顶给错了，就啥也不给人输出了
                // 有点不太合适 ^_^
                if (foundTop) {
                    List<WnObj> ans2 = new ArrayList<>(ans.size());
                    ans2.add(oTop);
                    while (it.hasNext()) {
                        ans2.add(it.next());
                    }
                    ans = ans2;
                }
            }

            // 不要顶
            if (excludeTop && !ans.isEmpty()) {
                ans.remove(0);
            }

            // 包括自己
            if (includeSelf) {
                ans.add(o);
            }

            // 计入到对象
            if (null != asKey) {
                o.put(asKey, ans);
            }

            // 计入结果
            res.add(ans);
        }

        // 要操作上下文了
        if (null == asKey) {
            // 清空上下文
            if (!params.is("keep")) {
                fc.clearAll();
            }

            // 设置上下文
            for (List<WnObj> objs : res) {
                fc.list.addAll(objs);
            }
        }
    }

}
