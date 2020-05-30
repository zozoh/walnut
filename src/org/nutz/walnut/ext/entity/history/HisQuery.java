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
    private String userId;
    private String userName;
    private String createTime;
    private String targetId;
    private String targetName;
    private String operation;
    private String more;

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
        this.joinStr(we, "uid", userId);
        this.joinStr(we, "unm", userName);
        this.joinStr(we, "tid", targetId);
        this.joinStr(we, "tnm", targetName);
        this.joinStr(we, "opt", operation);
        this.joinStr(we, "mor", more);
        this.joinRegion(we, "ct", createTime);

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMore() {
        return more;
    }

    public void setMore(String more) {
        this.more = more;
    }

}
