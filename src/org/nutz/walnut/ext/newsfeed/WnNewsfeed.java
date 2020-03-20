package org.nutz.walnut.ext.newsfeed;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

@Table("${t_newsfeed}")
public class WnNewsfeed {

    // ------------------------------------------
    // 消息标识
    // ------------------------------------------

    /**
     * 消息唯一 ID
     */
    @Name
    private String id;

    /**
     * 消息类型:
     */
    @Column("tp")
    private WnNewsfeedType type;

    // ------------------------------------------
    // 状态: 联合索引 read-star 将用来快速获取可清除消息
    // ------------------------------------------

    /**
     * 消息已读状态: 0.未读; 1.已读
     */
    @Column
    private boolean read;

    /**
     * 消息标记状态: 0.普通; 1.加星
     */
    @Column
    private boolean star;

    // ------------------------------------------
    // 时间戳
    // ------------------------------------------

    @Column("ct")
    private long createTime;

    @Column("rd_at")
    private long readAt;

    // ------------------------------------------
    // 消息的分发
    // ------------------------------------------
    @Column("src_id")
    private String sourceId;

    @Column("src_tp")
    private String sourceType;

    @Column("ta_id")
    private String targetId;

    @Column("ta_tp")
    private String targetType;

    // ------------------------------------------
    // 消息正文
    // ------------------------------------------

    @Column
    private String title;

    @Column
    private String content;

    // ------------------------------------------
    // 10个扩展字段
    // ------------------------------------------
    @Column("ext0")
    private String ext0;

    @Column("ext1")
    private String ext1;

    @Column("ext2")
    private String ext2;

    @Column("ext3")
    private String ext3;

    @Column("ext4")
    private String ext4;

    @Column("ext5")
    private String ext5;

    @Column("ext6")
    private String ext6;

    @Column("ext7")
    private String ext7;

    @Column("ext8")
    private String ext8;

    @Column("ext9")
    private String ext9;

    // ------------------------------------------
    // 帮助函数
    // ------------------------------------------

    public WnNewsfeed autoComplete(boolean forCreate) {
        // 默认为单播
        if (null == this.type) {
            this.type = WnNewsfeedType.UNICAST;
        }
        // 如果是创建的话，强制设置一下
        if (forCreate) {
            this.createTime = System.currentTimeMillis();
            this.readAt = 0;
            // 默认未读
            this.read = false;
            // 默认未星
            this.star = false;
        }
        // 创建时间
        else if (this.createTime <= 0) {
            this.createTime = System.currentTimeMillis();
        }

        // 返回自身以便链式赋值
        return this;
    }

    public WnNewsfeed clone() {
        WnNewsfeed feed = new WnNewsfeed();
        feed.id = this.id;
        feed.type = this.type;
        feed.read = this.read;
        feed.star = this.star;
        feed.createTime = this.createTime;
        feed.readAt = this.readAt;
        feed.sourceId = this.sourceId;
        feed.sourceType = this.sourceType;
        feed.targetId = this.targetId;
        feed.targetType = this.targetType;
        feed.title = this.title;
        feed.content = this.content;
        feed.ext0 = this.ext0;
        feed.ext1 = this.ext1;
        feed.ext2 = this.ext2;
        feed.ext3 = this.ext3;
        feed.ext4 = this.ext4;
        feed.ext5 = this.ext5;
        feed.ext6 = this.ext6;
        feed.ext7 = this.ext7;
        feed.ext8 = this.ext8;
        feed.ext9 = this.ext9;
        return feed;
    }

    public String toString() {
        return String.format("%s:%s[%s]->%s[%s]:%s",
                             id,
                             sourceType,
                             sourceId,
                             targetType,
                             targetId,
                             content);
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

    public WnNewsfeedType getType() {
        return type;
    }

    public void setType(WnNewsfeedType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getReadAt() {
        return readAt;
    }

    public void setReadAt(long readAt) {
        this.readAt = readAt;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExt0() {
        return ext0;
    }

    public void setExt0(String ext0) {
        this.ext0 = ext0;
    }

    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    public String getExt2() {
        return ext2;
    }

    public void setExt2(String ext2) {
        this.ext2 = ext2;
    }

    public String getExt3() {
        return ext3;
    }

    public void setExt3(String ext3) {
        this.ext3 = ext3;
    }

    public String getExt4() {
        return ext4;
    }

    public void setExt4(String ext4) {
        this.ext4 = ext4;
    }

    public String getExt5() {
        return ext5;
    }

    public void setExt5(String ext5) {
        this.ext5 = ext5;
    }

    public String getExt6() {
        return ext6;
    }

    public void setExt6(String ext6) {
        this.ext6 = ext6;
    }

    public String getExt7() {
        return ext7;
    }

    public void setExt7(String ext7) {
        this.ext7 = ext7;
    }

    public String getExt8() {
        return ext8;
    }

    public void setExt8(String ext8) {
        this.ext8 = ext8;
    }

    public String getExt9() {
        return ext9;
    }

    public void setExt9(String ext9) {
        this.ext9 = ext9;
    }

}
