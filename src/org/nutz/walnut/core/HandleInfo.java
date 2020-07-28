package org.nutz.walnut.core;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Strings;

/**
 * 一个句柄的纯信息类，比较方便持久化...呃，再想想
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class HandleInfo {

    /**
     * 句柄唯一 ID
     */
    private String id;

    /**
     * 句柄模式
     */
    private int mode;

    /**
     * 句柄关联的目标对象ID（可以从索引管理器取回WnObj完整信息）
     */
    private String targetId;

    /**
     * 如果本对象是映射对象，那么这个字段存放其顶端映射对象的ID
     * <p>
     * 映射工厂接口需要这个ID以便获取这个映射的完整信息
     */
    private String homeId;

    /**
     * 关联对象映射。以便持久化后，取回桶管理器和索引管理器实例
     */
    private String mount;

    /**
     * 句柄游标偏移量的值
     */
    protected long offset;

    /**
     * 句柄创建时间（绝对毫秒）
     */
    private long creatTime;

    /**
     * 句柄生命周期（毫秒）
     */
    private long timeout;

    /**
     * 句柄过期时间（绝对毫秒）
     */
    private long expiTime;

    public boolean hasId() {
        return !Strings.isBlank(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(long creatTime) {
        this.creatTime = creatTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public boolean hasTimeout() {
        return timeout > 0;
    }

    public int getTimeoutInSecond() {
        return (int) timeout / 1000;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getExpiTime() {
        return expiTime;
    }

    public void setExpiTime(long expiTime) {
        this.expiTime = expiTime;
    }

    public void updateBy(HandleInfo info) {
        this.id = info.id;
        this.mode = info.mode;
        this.targetId = info.targetId;
        this.mount = info.mount;
        this.offset = info.offset;
        this.creatTime = info.creatTime;
        this.timeout = info.timeout;
        this.expiTime = info.expiTime;
    }

    public Map<String, String> toStringMap() {
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("mode", mode + "");
        map.put("targetId", targetId);
        if (null != mount) {
            map.put("mount", mount);
        }
        map.put("offset", offset + "");
        map.put("creatTime", creatTime + "");
        map.put("timeout", timeout + "");
        map.put("expiTime", expiTime + "");
        return map;
    }

    public Map<String, String> toStringTouchMap() {
        Map<String, String> map = new HashMap<>();
        map.put("offset", offset + "");
        map.put("timeout", timeout + "");
        map.put("expiTime", expiTime + "");
        return map;
    }
}
