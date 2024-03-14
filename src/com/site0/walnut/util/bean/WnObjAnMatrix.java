package com.site0.walnut.util.bean;

import java.util.List;

import com.site0.walnut.api.io.WnObj;

public class WnObjAnMatrix {

    public int len;
    public WnObj[][] matrix;
    public int nMax;
    public int nMin;

    public WnObjAnMatrix() {}

    public WnObjAnMatrix(List<WnObj> objs) {
        this.set(objs);
    }

    public void set(List<WnObj> objs) {
        if (null != objs && !objs.isEmpty()) {
            len = objs.size();
            matrix = new WnObj[len][];
            nMax = Integer.MIN_VALUE;
            nMin = Integer.MAX_VALUE;
            int i = 0;
            for (WnObj o : objs) {
                List<WnObj> list = o.parents();
                list.add(o);
                WnObj[] ary = new WnObj[list.size()];
                list.toArray(ary);
                matrix[i++] = ary;
                nMax = Math.max(nMax, ary.length);
                nMin = Math.min(nMin, ary.length);
            }
        }
    }

    public WnObj findCommonParent(WnObj oTop) {
        int pN = findCommonParentIndex(oTop);

        // 得到公共根节点
        if (pN < 0) {
            return null;
        }
        return matrix[0][pN];
    }

    public int findCommonParentIndex(WnObj oTop) {
        for (int n = 0; n < nMin; n++) {
            WnObj o0 = matrix[0][n];
            if (null != oTop && o0.isSameId(oTop)) {
                return n;
            }
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

    public void reset() {
        len = 0;
        matrix = null;
        nMax = Integer.MIN_VALUE;
        nMin = Integer.MAX_VALUE;
    }

}
