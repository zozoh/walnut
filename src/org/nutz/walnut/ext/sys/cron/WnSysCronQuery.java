package org.nutz.walnut.ext.sys.cron;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.ZParams;

public class WnSysCronQuery {

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
        if (null != userName) {
            q.setv("user", userName);
        }
        if (skip > 0) {
            q.skip(skip);
        }
        if (limit > 0) {
            q.limit(limit);
        }
    }

    public void loadFromParams(ZParams params) {
        // 处理用户信息
        if (params.has("u")) {
            this.userName = params.getString("u", null);
        }
        // 处理游标限制
        this.skip = params.getInt("skip", 0);
        this.limit = params.getInt("limit", 0);
    }

    public String toString() {
        return String.format("@%s:%d/%d", userName, skip, limit);
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
