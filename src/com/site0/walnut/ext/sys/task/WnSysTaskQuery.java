package com.site0.walnut.ext.sys.task;

import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.util.ZParams;

public class WnSysTaskQuery {

    /**
     * 指定要列出的任务类型，如果不填写，则针对全部类型
     */
    private WnSysTaskType type;

    /**
     * 指定任务的 ID 列表
     */
    private List<String> ids;

    /**
     * 仅列出某个用户的任务
     */
    private String userName;

    /**
     * 跳过多少记录
     */
    private int skip;

    /**
     * 最多列出多少记录
     */
    private int limit;

    public void joinQuery(WnQuery q) {
        if (null != type) {
            q.setv("tp", type.name().toLowerCase());
        }
        if (null != userName) {
            q.setv("c", userName);
        }
        if (skip > 0) {
            q.skip(skip);
        }
        if (limit > 0) {
            q.limit(limit);
        }
    }

    public void loadFromParams(ZParams params) {
        // 类型就看第一个是不是
        if (params.vals.length > 0) {
            // 第一个值不是类型的话，就是 ID 咯
            String v = params.val(0);
            if (v.matches("^(cron|task)$")) {
                this.setType(v);
            } else {
                this.addIds(v);
            }
            // 剩下的按照 ID 读
            for (int i = 1; i < params.vals.length; i++) {
                v = params.val(i);
                this.addIds(v);
            }
        }
        // 处理用户信息
        if (params.has("u")) {
            this.userName = params.getString("u", null);
        }
        // 处理游标限制
        this.skip = params.getInt("skip", 0);
        this.limit = params.getInt("limit", 0);
    }

    public String toString() {
        return String.format("[%s]@%s:%d/%d", type, userName, skip, limit);
    }

    public WnSysTaskType getType() {
        return type;
    }

    public void setType(WnSysTaskType type) {
        this.type = type;
    }

    public void setType(String type) {
        if (null == type) {
            this.type = null;
        } else {
            String ts = type.toUpperCase();
            this.type = WnSysTaskType.valueOf(ts);
        }
    }

    public boolean hasIds() {
        return null != ids && !ids.isEmpty();
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public void addIds(String... ids) {
        if (null == this.ids) {
            this.ids = new LinkedList<>();
        }
        for (String id : ids) {
            this.ids.add(id);
        }
    }

    public boolean hasUserName() {
        return null != userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
