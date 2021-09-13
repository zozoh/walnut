package org.nutz.walnut.ext.util.react.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ReactConfig {

    private List<ReactItem> items;

    public ReactConfig() {
        this.items = new LinkedList<>();
    }

    public void addItems(ReactItem... items) {
        for (ReactItem it : items) {
            // 看看是否需要替换
            if (it.isOverride()) {
                boolean found = false;
                ListIterator<ReactItem> liIt = this.items.listIterator();
                while (liIt.hasNext()) {
                    ReactItem li = liIt.next();
                    if (li.isSameName(it)) {
                        liIt.set(li);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }
            // 直接加入
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
