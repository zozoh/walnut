package org.nutz.walnut.core;

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
     * 关联对象映射。以便持久化后，取回桶管理器和索引管理器实例
     */
    private String mount;

    /**
     * 句柄创建时间
     */
    private long creatTime;

    /**
     * 句柄游标偏移量的值
     */
    private long offset;

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

    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    public long getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(long creatTime) {
        this.creatTime = creatTime;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void updateBy(HandleInfo info) {
        this.id = info.id;
        this.mode = info.mode;
        this.targetId = info.targetId;
        this.creatTime = info.creatTime;
        this.offset = info.offset;
    }

}
