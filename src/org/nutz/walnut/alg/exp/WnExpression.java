package org.nutz.walnut.alg.exp;

import java.util.List;

public class WnExpression {

    private List<WnExpItem> items;

    public WnExpression() {}

    public WnExpression(List<WnExpItem> items) {
        this.items = items;
    }

    public List<WnExpItem> getItems() {
        return items;
    }

    public void setItems(List<WnExpItem> items) {
        this.items = items;
    }

}
