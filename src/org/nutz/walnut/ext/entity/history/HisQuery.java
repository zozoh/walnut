package org.nutz.walnut.ext.entity.history;

import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.entity.DaoEntityQuery;

public class HisQuery extends DaoEntityQuery {

    // ------------------------------------------
    // 查询条件
    // ------------------------------------------
    private String id;
    private String uid;
    private String unm;
    private String ct;
    private String tid;
    private String tnm;
    private String opt;
    private String mor;

    /**
     * @return SQL 的查询条件
     */
    public Condition toCondition() {
        // 定义了 ID，其他就不看了
        if (!Strings.isBlank(id)) {
            return Cnd.where("id", "=", id);
        }

        // ........................................
        // 准备条件
        SimpleCriteria cri = Cnd.cri();
        SqlExpressionGroup we = cri.where();

        this.joinStr(we, "id", id);
        this.joinStr(we, "uid", uid);
        this.joinStr(we, "unm", unm);
        this.joinStr(we, "tid", tid);
        this.joinStr(we, "tnm", tnm);
        this.joinStr(we, "opt", opt);
        this.joinStr(we, "mor", mor);
        this.joinRegion(we, "ct", ct);

        // // ........................................
        // 排序
        joinSorts(cri);

        // ........................................
        // 搞定
        return cri;
    }

    // ------------------------------------------
    // Getter / Setter
    // ------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String userId) {
        this.uid = userId;
    }

    public String getUnm() {
        return unm;
    }

    public void setUnm(String userName) {
        this.unm = userName;
    }

    public String getCt() {
        return ct;
    }

    public void setCt(String createTime) {
        this.ct = createTime;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String targetId) {
        this.tid = targetId;
    }

    public String getTnm() {
        return tnm;
    }

    public void setTnm(String targetName) {
        this.tnm = targetName;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String operation) {
        this.opt = operation;
    }

    public String getMor() {
        return mor;
    }

    public void setMor(String more) {
        this.mor = more;
    }

}
