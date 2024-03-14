package com.site0.walnut.ext.data.entity.history.fake;

import java.util.HashMap;

public class HistoryFakeConfig {

    private HashMap<String, HistoryFakeSchema> schema;

    private HistoryFakeField id;

    private HistoryFakeField userId;

    private HistoryFakeField userName;

    private HistoryFakeField userType;

    private HistoryFakeField targetId;

    private HistoryFakeField targetName;

    private HistoryFakeField targetType;

    private HistoryFakeField operation;

    private HistoryFakeField more;

    public boolean hasSchema() {
        return null != this.schema && this.schema.size() > 0;
    }

    public HashMap<String, HistoryFakeSchema> getSchema() {
        return schema;
    }

    public void setSchema(HashMap<String, HistoryFakeSchema> schema) {
        this.schema = schema;
    }

    public HistoryFakeField getId() {
        return id;
    }

    public void setId(HistoryFakeField id) {
        this.id = id;
    }

    public HistoryFakeField getUserId() {
        return userId;
    }

    public void setUserId(HistoryFakeField userId) {
        this.userId = userId;
    }

    public HistoryFakeField getUserName() {
        return userName;
    }

    public void setUserName(HistoryFakeField userName) {
        this.userName = userName;
    }

    public HistoryFakeField getUserType() {
        return userType;
    }

    public void setUserType(HistoryFakeField userType) {
        this.userType = userType;
    }

    public HistoryFakeField getTargetId() {
        return targetId;
    }

    public void setTargetId(HistoryFakeField targetId) {
        this.targetId = targetId;
    }

    public HistoryFakeField getTargetName() {
        return targetName;
    }

    public void setTargetName(HistoryFakeField targetName) {
        this.targetName = targetName;
    }

    public HistoryFakeField getTargetType() {
        return targetType;
    }

    public void setTargetType(HistoryFakeField targetType) {
        this.targetType = targetType;
    }

    public HistoryFakeField getOperation() {
        return operation;
    }

    public void setOperation(HistoryFakeField operation) {
        this.operation = operation;
    }

    public HistoryFakeField getMore() {
        return more;
    }

    public void setMore(HistoryFakeField more) {
        this.more = more;
    }

}
