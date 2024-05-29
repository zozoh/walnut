package com.site0.walnut.val.util;

/**
 * 描述了一个序列的信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSeqInfo {

    /**
     * 逻辑上的名称空间， 譬如对于 <code>WnObjSeqMaker</code> 这个就是 parentPath
     */
    private String scope;

    /**
     * 存储序列内容的目标 譬如对于 <code>WnObjSeqMaker</code> 这个就是 fileNmae
     */
    private String target;

    /**
     * 序列值存储的字段名 譬如对于 <code>WnObjSeqMaker</code> 这个就是 key
     */
    private String name;

    public WnSeqInfo() {}

    public WnSeqInfo(String scope, String target, String name) {
        this.scope = scope;
        this.target = target;
        this.name = name;
    }

    /**
     * 将一个字符串，解析成本结构。这个字符串的格式如下：
     * 
     * <pre>
     * ~/path/to/yyyy-MM-dd.'txt'#my_seq
     *   |        |                   |-- name = my_seq 
     *   |        |
     *   |        +-- target = yyyy-MM-dd.'txt' 
     *   | 
     *   +-- scope = ~/path/to
     * </pre>
     *
     * @param str
     * @return
     */
    public WnSeqInfo valueOf(String str) {
        int pos = str.lastIndexOf('#');
        if (pos >= 0) {
            this.name = str.substring(pos + 1).trim();
            str = str.substring(0, pos);
        }

        pos = str.lastIndexOf('/');
        if (pos >= 0) {
            this.target = str.substring(pos + 1).trim();
            this.scope = str.substring(0, pos).trim();
        }
        // 默认全都给 scope
        else {
            this.scope = str.trim();
        }

        return this;

    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
