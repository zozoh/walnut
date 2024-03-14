package com.site0.walnut.ext.data.o.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

/**
 * 一个用来分析一组文件父节点的帮助类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnObjMatrix {

    public int nMax = Integer.MIN_VALUE;
    public int nMin = Integer.MAX_VALUE;
    public int len;
    public int pN;
    public WnObj oBase;
    public WnObj oTop;
    public WnObj[][] matrix;
    public List<WnObj> objs;

    public WnObjMatrix() {}

    public WnObjMatrix(WnObj oBase) {
        this.oBase = oBase;
    }

    public WnObjMatrix(WnObj oBase, List<WnObj> list) {
        this.oBase = oBase;
        this.load(list);
    }

    public WnObjMatrix load(List<WnObj> list) {
        // 如果声明了 oBase，则预先执行一下过滤
        // 防守
        if (null == list || list.isEmpty()) {
            this.objs = new LinkedList<>();
        }
        // 看看是否需要过滤
        else {
            this.objs = new ArrayList<>(list.size());
            // 保持
            if (null == oBase) {
                this.objs.addAll(list);
            }
            // 过滤
            else {
                String basePath = oBase.getRegularPath();
                for (WnObj o : list) {
                    String aph = o.getRegularPath();
                    if (aph.startsWith(basePath)) {
                        this.objs.add(o);
                    }
                }
            }
        }

        // 预备 ...
        this.len = this.objs.size();
        matrix = new WnObj[len][];
        // ...........................................
        // 首先读取所有对象的祖先, 形成一个二维数组
        // :[i][n]
        int i = 0;
        for (WnObj o : this.objs) {
            List<WnObj> axis = o.parents();
            axis.add(o);
            WnObj[] ary = new WnObj[axis.size()];
            axis.toArray(ary);
            matrix[i++] = ary;
            nMax = Math.max(nMax, ary.length);
            nMin = Math.min(nMin, ary.length);
        }
        // ...........................................
        // 自动寻找公共的父
        this.pN = __find_common_pIndex(matrix, len, nMin, oBase);
        // ...........................................
        // 得到树的根节点
        // 没有公共的父，那就是根节点咯
        if (pN < 0) {
            this.oTop = null;
        } else {
            this.oTop = matrix[0][pN];
        }
        // 搞定
        return this;
    }

    int __find_common_pIndex(WnObj[][] matrix, int len, int nMin, WnObj oTop) {
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

    public WnObj getTopOrRoot(WnObj root) {
        if (null == this.oTop) {
            return root.clone();
        }
        return this.oTop;
    }

    public WnObj getTopOrRoot(WnIo io) {
        return this.getTopOrRoot(io.getRoot());
    }
}
