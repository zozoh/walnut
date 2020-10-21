package org.nutz.walnut.ext.entity.statistics.bean;

public class NamedAmsRange extends AmsRange {

    private String name;

    public NamedAmsRange() {}

    public NamedAmsRange(String name, long beginInMs, long endInMs) {
        super(beginInMs, endInMs);
        this.name = name;
    }

    public String toString() {
        return String.format("%s(%s/%s)",
                             this.name,
                             this.formatBegin("yyyy-MM-dd"),
                             this.formatEnd("yyyy-MM-dd"));
    }

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

}
