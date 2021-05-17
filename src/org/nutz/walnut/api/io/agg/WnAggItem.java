package org.nutz.walnut.api.io.agg;

/**
 * 聚集函数执行的结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAggItem {

    /**
     * 聚集键
     */
    private String name;

    /**
     * 聚集值
     */
    private long value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

}
