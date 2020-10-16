package org.nutz.walnut.ext.entity.statistics.bean;

public class NamedAmsRange extends AmsRange {

    private String name;

    public NamedAmsRange() {}

    public NamedAmsRange(String name, long beginInMs, long endInMs) {
        super(beginInMs, endInMs);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String title) {
        this.name = title;
    }

}
