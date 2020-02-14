package org.nutz.walnut.ext.bulk.api;

import static org.nutz.walnut.ext.bulk.api.BulkRLTree.*;
import static org.nutz.walnut.ext.bulk.api.BulkRLObj.*;

public class BulkRestoreSetting {

    public BulkRLTree BSR;
    public BulkRLTree BDR;
    public BulkRLTree PKG;
    public BulkRLTree TREE;

    public BulkRLObj path;
    public BulkRLObj oid;
    public BulkRLObj rid;
    public BulkRLObj biz;
    public BulkRLObj mod;
    public BulkRLObj ln;

    public BulkRestoreSetting asTree1() {
        return this.asTree(R, I, A, I);
    }

    public BulkRestoreSetting asTree2() {
        return this.asTree(R, R, A, I);
    }

    public BulkRestoreSetting asTree3() {
        return this.asTree(R, R, A, D);
    }

    public BulkRestoreSetting asObj1() {
        return this.asObj(rl, nw, tr, ig, ig, ig);
    }

    public BulkRestoreSetting asObj2() {
        return this.asObj(rl, nw, tr, cp, ig, tr);
    }

    public BulkRestoreSetting asObj3() {
        return this.asObj(rl, nw, tr, cp, cp, tr);
    }

    public BulkRestoreSetting asObj4() {
        return this.asObj(cp, cp, cp, cp, cp, cp);
    }

    public BulkRestoreSetting asTree(BulkRLTree BSR,
                                     BulkRLTree BDR,
                                     BulkRLTree PKG,
                                     BulkRLTree TREE) {
        this.BSR = BSR;
        this.BDR = BDR;
        this.PKG = PKG;
        this.TREE = TREE;
        return this;
    }

    public BulkRestoreSetting asObj(BulkRLObj path,
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
