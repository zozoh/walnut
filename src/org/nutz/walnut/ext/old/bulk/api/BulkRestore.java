package org.nutz.walnut.ext.old.bulk.api;

import static org.nutz.walnut.ext.old.bulk.api.BulkRLObj.*;
import static org.nutz.walnut.ext.old.bulk.api.BulkRLTree.*;

/**
 * 定义了恢复时的恢复级别设置
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class BulkRestore {

    // 增删级别
    public BulkRLTree BSR;
    public BulkRLTree BDR;
    public BulkRLTree PKG;
    public BulkRLTree TREE;

    // 修改级别
    public BulkRLObj path;
    public BulkRLObj oid;
    public BulkRLObj rid;
    public BulkRLObj biz;
    public BulkRLObj mod;
    public BulkRLObj ln;

    public BulkRestore asTree1() {
        return this.asTree(R, I, A, I);
    }

    public BulkRestore asTree2() {
        return this.asTree(R, R, A, I);
    }

    public BulkRestore asTree3() {
        return this.asTree(R, R, A, D);
    }

    public BulkRestore asObj1() {
        return this.asObj(rl, nw, tr, ig, ig, ig);
    }

    public BulkRestore asObj2() {
        return this.asObj(rl, nw, tr, cp, ig, tr);
    }

    public BulkRestore asObj3() {
        return this.asObj(rl, nw, tr, cp, cp, tr);
    }

    public BulkRestore asObj4() {
        return this.asObj(cp, cp, cp, cp, cp, cp);
    }

    public BulkRestore asTree(BulkRLTree BSR,
                                     BulkRLTree BDR,
                                     BulkRLTree PKG,
                                     BulkRLTree TREE) {
        this.BSR = BSR;
        this.BDR = BDR;
        this.PKG = PKG;
        this.TREE = TREE;
        return this;
    }

    public BulkRestore asObj(BulkRLObj path,
                                    BulkRLObj oid,
                                    BulkRLObj rid,
                                    BulkRLObj biz,
                                    BulkRLObj mod,
                                    BulkRLObj ln) {
        this.path = path;
        this.oid = oid;
        this.rid = rid;
        this.biz = biz;
        this.mod = mod;
        this.ln = ln;
        return this;
    }

}
