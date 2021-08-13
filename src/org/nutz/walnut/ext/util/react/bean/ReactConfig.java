package org.nutz.walnut.ext.util.react.bean;

import java.util.LinkedList;
import java.util.List;

public class ReactConfig {

    private List<ReactItem> items;

    public ReactConfig() {
        this.items = new LinkedList<>();
    }

    public void addItems(ReactItem... items) {
        for (ReactItem it : items) {
            this.items.add(it);
        }
    }

    public void clearItems() {
        items.clear();
    }

    public List<ReactItem> getItems() {
        return items;
    }

    public void setItems(List<ReactItem> items) {
        this.items = items;
    }
}
