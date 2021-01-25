package org.nutz.walnut.ext.o.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_tree extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(root)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 得到上下文对象的数量
        int len = fc.list.size();
        // ...........................................
        // 防守
        if (0 == len)
            return;
        // ...........................................
        // 首先读取所有对象的祖先, 形成一个二维数组
        // :[i][n]
        WnObj[][] matrix = new WnObj[len][];
        int nMax = Integer.MIN_VALUE;
        int nMin = Integer.MAX_VALUE;
        int i = 0;
        int n = 0;
        for (WnObj o : fc.list) {
            List<WnObj> list = o.parents();
            list.add(o);
            WnObj[] ary = new WnObj[list.size()];
            list.toArray(ary);
            matrix[i++] = ary;
            nMax = Math.max(nMax, ary.length);
            nMin = Math.min(nMin, ary.length);
        }
        // ...........................................
        // 自动寻找公共的父
        int pN = 0;
        if (!params.is("root")) {
            // 是否指定了树的公共顶级节点呢？
            String phTop = params.getString("top");
            WnObj oUsrTop = null;
            if (!Ws.isBlank(phTop)) {
                oUsrTop = Wn.checkObj(sys, phTop);
            }

            // 逐列寻找最后一个相同的父下标
            pN = __find_common_pIndex(matrix, len, nMin, oUsrTop);
        }
        // ...........................................
        // 得到树的根节点
        WnObj oTop;
        // 没有公共的父，那就是根节点咯
        if (pN < 0) {
            oTop = sys.io.getRoot().clone();
        } else {
            oTop = matrix[0][pN];
        }
        // ...........................................
        // 所有的节点都汇集到父
        String subkey = fc.subKey;
        List<WnObj> children = new ArrayList<>(len);
        for (i = 0; i < len; i++) {
            // 从 pN 开始，收缩起来
            WnObj[] axis = matrix[i];
            WnObj child = null;
            WnObj sub = null;
            for (n = pN + 1; n < axis.length; n++) {
                WnObj o = matrix[i][n];
                if (null != sub) {
                    sub.put(subkey, Lang.list(o));
                }
                sub = o;
                if (null == child) {
                    child = sub;
                }
            }
            // 记入结果
            if (null != child) {
                children.add(child);
            }
        }
        oTop.put(subkey, children);
        // ...........................................
        // 读取子节点
        int depth = params.getInt("depth", 0);
        if (depth > 0) {
            for (WnObj o : fc.list) {
                loadDescendants(sys.io, o, subkey, depth);
            }
        }
        // ...........................................
        // 更新上下文
        fc.clearAll();
        fc.add(oTop);
    }

    private int __find_common_pIndex(WnObj[][] matrix, int len, int nMin, WnObj oTop) {
        for (int n = 0; n < nMin; n++) {
            WnObj o0 = matrix[0][n];
            for (int i = 1; i < len; i++) {
                WnObj o = matrix[i][n];
                // 指定的公共树顶级节点
                if (null != oTop && oTop.isSameId(o)) {
                    return n;
                }
                // 一旦遇到不相同，则表示一定是树的公共顶级节点
                if (!o0.isSameId(o)) {
                    return n - 1;
                }
            }
        }
        return nMin - 1;
    }

    private void loadDescendants(WnIo io, WnObj o, String subkey, int depth) {
        // 不是目录，必然木有子节点
        if (!o.isDIR()) {
            return;
        }
        // 读自己的子节点
        List<WnObj> children = io.getChildren(o, null);
        o.put(subkey, children);
        // 还有必要读下去吗？
        if (depth > 1 && !children.isEmpty()) {
            for (WnObj child : children) {
                loadDescendants(io, child, subkey, depth - 1);
            }
        }
    }

}
