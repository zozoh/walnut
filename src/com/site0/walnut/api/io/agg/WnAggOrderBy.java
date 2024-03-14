package com.site0.walnut.api.io.agg;

public class WnAggOrderBy {

    private String name;

    private boolean asc;

    public WnAggOrderBy() {}

    public WnAggOrderBy(String name, boolean asc) {
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
