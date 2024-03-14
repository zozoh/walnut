package com.site0.walnut.ext.data.entity.buy;

import com.site0.walnut.api.io.WnObj;

public class BuyIt {

    private String name;

    private int count;

    private WnObj obj;

    public BuyIt() {}

    public BuyIt(String target, int count) {
        this.setName(target);
        this.setCount(count);
    }

    public String getName() {
        return name;
    }

    public void setName(String target) {
        this.name = target;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
    }

    public String toString() {
        return String.format("%s:%d", name, count);
    }

}
