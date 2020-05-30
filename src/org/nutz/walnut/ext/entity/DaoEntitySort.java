package org.nutz.walnut.ext.entity;

public class DaoEntitySort {

    private String name;

    private boolean asc;

    public DaoEntitySort() {}

    public DaoEntitySort(String name, boolean asc) {
        this.name = name;
        this.asc = asc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

}
