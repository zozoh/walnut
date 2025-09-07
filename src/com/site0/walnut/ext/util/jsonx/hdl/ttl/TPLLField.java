package com.site0.walnut.ext.util.jsonx.hdl.ttl;

public class TPLLField {

    private String key;

    private TPLLFieldType type;

    /**
     * 字段开始位置（1base）
     */
    private int start;

    /**
     * 字段长度
     */
    private int len;

    private String comments;

    public boolean isFiller() {
        return "Filler".equalsIgnoreCase(this.key);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isNumeric() {
        return TPLLFieldType.Numeric == this.type;
    }

    public boolean isCharacter() {
        return TPLLFieldType.Character == this.type;
    }

    public TPLLFieldType getType() {
        return type;
    }

    public void setType(TPLLFieldType type) {
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
