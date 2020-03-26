package org.nutz.walnut.ext.newsfeed;

import java.util.LinkedList;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.lang.Strings;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.walnut.util.WnRg;

public class FeedQuery {

    // ------------------------------------------
    // 查询条件
    // ------------------------------------------
    private String id;
    private FeedType type;
    private Boolean readed;
    private Boolean stared;
    private String createTime;
    private String readAt;
    private String sourceId;
    private String sourceType;
    private String targetId;
    private String targetType;
    private String title;
    private String content;
    // 10个扩展字段
    private String ext0;
    private String ext1;
    private String ext2;
    private String ext3;
    private String ext4;
    private String ext5;
    private String ext6;
    private String ext7;
    private String ext8;
    private String ext9;

    // ------------------------------------------
    // 排序
    // ------------------------------------------
    private List<FeedSort> sorts;

    /**
     * @return
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

        // 指定类型
        if (null != type) {
            we.andEquals("tp", type.ordinal());
        }

        // 已读/标记
        this.__join_bool(we, "readed", readed);
        this.__join_bool(we, "stared", stared);

        // 时间戳
        this.__join_region(we, "ct", createTime);
        this.__join_region(we, "rd_at", readAt);

        // 源
        this.__join_str(we, "src_id", sourceId);
        this.__join_str(we, "src_tp", sourceType);

        // 消息发送目标（接收者）
        this.__join_str(we, "ta_id", targetId);
        this.__join_str(we, "ta_tp", targetType);

        // 消息标题/正文
        this.__join_like(we, "title", title);
        this.__join_like(we, "content", content);

        // 10个扩展字段
        this.__join_str(we, "ext0", ext0);
        this.__join_str(we, "ext1", ext1);
        this.__join_str(we, "ext2", ext2);
        this.__join_str(we, "ext3", ext3);
        this.__join_str(we, "ext4", ext4);
        this.__join_str(we, "ext5", ext5);
        this.__join_str(we, "ext6", ext6);
        this.__join_str(we, "ext7", ext7);
        this.__join_str(we, "ext8", ext8);
        this.__join_str(we, "ext9", ext9);

        // ........................................
        // 排序
        if (null != sorts) {
            for (FeedSort sr : sorts) {
                if (sr.isAsc()) {
                    cri.asc(sr.getName());
                } else {
                    cri.desc(sr.getName());
                }
            }
        }

        // ........................................
        // 搞定
        return cri;
    }

    private void __join_like(SqlExpressionGroup we, String name, String val) {
        if (!Strings.isBlank(val)) {
            we.andLike(name, val);
        }
    }

    private void __join_region(SqlExpressionGroup we, String name, String str) {
        if (!Strings.isBlank(str)) {
            String s = WnRg.extend_rg_macro(str);
            LongRegion rg = Region.Long(s);
            // Region
            if (rg.isRegion()) {
                // [xxx, )
                if (null == rg.right()) {
                    if (rg.isLeftOpen()) {
                        we.andGT(name, rg.left());
                    } else {
                        we.andGTE(name, rg.left());
                    }
                }
                // (, xxx)
                else if (null == rg.left()) {
                    if (rg.isRightOpen()) {
                        we.andLT(name, rg.right());
                    } else {
                        we.andLTE(name, rg.right());
                    }
                }
                // [xxx, xxx]
                else {
                    we.andBetween(name, rg.left(), rg.right());
                }
            }
        }
    }

    private void __join_str(SqlExpressionGroup we, String name, String val) {
        if (!Strings.isBlank(val)) {
            we.andEquals(name, val);
        }
    }

    private void __join_bool(SqlExpressionGroup we, String name, Boolean val) {
        if (null != val) {
            we.andEquals(name, val);
        }
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

    public FeedType getType() {
        return type;
    }

    public void setType(FeedType type) {
        this.type = type;
    }

    public Boolean getReaded() {
        return readed;
    }

    public void setReaded(Boolean readed) {
        this.readed = readed;
    }

    public Boolean getStared() {
        return stared;
    }

    public void setStared(Boolean stared) {
        this.stared = stared;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
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

    public List<FeedSort> getSorts() {
        return sorts;
    }

    public void setSorts(List<FeedSort> sorts) {
        this.sorts = sorts;
    }

    public void setSorts(NutMap sort) {
        if (!sort.isEmpty()) {
            for (String key : sort.keySet()) {
                int val = sort.getInt(key);
                this.addSort(key, val == 1);
            }
        }
    }

    public void addSort(FeedSort sort) {
        if (null == this.sorts) {
            this.sorts = new LinkedList<>();
        }
        this.sorts.add(sort);
    }

    public void addSort(String name, boolean asc) {
        this.addSort(new FeedSort(name, asc));
    }
}
