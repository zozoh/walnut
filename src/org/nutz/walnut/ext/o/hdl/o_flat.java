package org.nutz.walnut.ext.o.hdl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_flat extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(leaf|ignore)$");
    }

    static class Flating {
        WnSystem sys;
        String childBy;
        String[] tagBy;
        String tagKey;
        boolean leaf;
        LinkedList<String> tagStack;

        List<WnObj> list;

        void doFlat(List<WnObj> objs) {
            for (WnObj o : objs) {
                List<WnObj> children = o.getAsList(this.childBy, WnObj.class);
                // 没有 Children 则认为是叶子节点
                if (null == children) {
                    this.pushObj(o);
                }
                // 中间节点的话，看看是否需要推入
                else {
                    if (!this.leaf) {
                        this.pushObj(o);
                    }
                    // 看看能否找到标签键
                    Object tag = o.getOrBy(tagBy, null);
                    if (null != tag) {
                        tagStack.push(tag.toString());
                        // 递归进去
                        this.doFlat(children);
                        // 然后弹出自己的标签
                        tagStack.pop();
                    }
                    // 那就是无标签咯，直接递归进去就好
                    else {
                        this.doFlat(children);
                    }
                }
            }
        }

        void pushObj(WnObj o) {
            if (!tagStack.isEmpty() && null != tagKey) {
                ArrayList<String> tags = new ArrayList<>(tagStack.size());
                tags.addAll(tagStack);
                o.put(tagKey, tags);
            }
            list.add(o);
        }
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 有时候（譬如拼装 API指令时，支持一个 -ignore 比较方便
        if (params.is("ignore"))
            return;

        // 分析参数
        Flating ing = new Flating();
        ing.sys = sys;
        ing.childBy = params.getString("by", "children");
        ing.tagBy = Ws.splitIgnoreBlank(params.getString("tagby"));
        ing.tagKey = params.getString("tagkey", "tag");
        ing.leaf = params.is("leaf", false);
        ing.list = new LinkedList<>();
        ing.tagStack = new LinkedList<>();
        ing.doFlat(fc.list);
        fc.list = ing.list;
    }

}
