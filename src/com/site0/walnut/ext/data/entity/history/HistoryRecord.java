package com.site0.walnut.ext.data.entity.history;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.json.JsonField;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;

@Table("${t_history}")
public class HistoryRecord {

    /**
     * 历史记录唯一 ID
     */
    @Name
    private String id;

    /**
     * 用户ID
     */
    @Column("uid")
    @JsonField("uid")
    private String userId;

    /**
     * 【冗】用户名
     */
    @Column("unm")
    @JsonField("unm")
    private String userName;

    /**
     * 【选】用户类型
     */
    @Column("utp")
    @JsonField("utp")
    private String userType;

    @Column("ct")
    @JsonField("ct")
    private long createTime;

    /**
     * 关联对象的 ID
     */
    @Column("tid")
    @JsonField("tid")
    private String targetId;

    /**
     * 【冗】关联对象名
     */
    @Column("tnm")
    @JsonField("tnm")
    private String targetName;

    /**
     * 【选】关联对象类型
     */
    @Column("ttp")
    @JsonField("ttp")
    private String targetType;

    /**
     * 动作名称
     */
    @Column("opt")
    @JsonField("opt")
    private String operation;

    /**
     * 动作更多细节
     */
    @Column("mor")
    @JsonField("mor")
    private String more;

    public String toString() {
        Date d = Times.D(this.createTime);
        String ds = Times.format("yyyy-MM-dd HH:mm:ss.SSS", d);
        return String.format("%s:U(%s)@%sDO(%s)->%s:%s",
                             id,
                             userId,
                             ds,
                             operation,
                             targetId,
                             targetType);
    }

    public NutMap toBean() {
        NutMap map = new NutMap();
        map.put("id", id);
        map.put("uid", userId);
        map.put("unm", userName);
        map.put("utp", userType);
        map.put("ct", createTime);
        map.put("cts", Times.format("yyyy-MM-dd HH:mm:ss.SSS", new Date(createTime)));
        map.put("tid", targetId);
        map.put("tnm", targetName);
        map.put("ttp", targetType);
        map.put("opt", operation);
        map.put("mor", more);
        return map;
    }

    public boolean hasId() {
        return null != id;
    }

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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean hasCreateTime() {
        return createTime > 0;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
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
