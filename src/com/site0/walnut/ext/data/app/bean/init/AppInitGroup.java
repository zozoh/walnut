package com.site0.walnut.ext.data.app.bean.init;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;

public class AppInitGroup {

    private String title;

    private List<AppInitItem> items;

    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof AppInitGroup) {
            AppInitGroup group = (AppInitGroup) obj;

            // 标题
            if (this.hasTitle()) {
                if (group.hasTitle() && !title.equals(group.title)) {
                    return false;
                }
            } else if (group.hasTitle()) {
                return false;
            }

            // 项目
            if (this.hasItems()) {
                if (group.hasItems()) {
                    if (items.size() != group.items.size()) {
                        return false;
                    }
                    Iterator<AppInitItem> it0 = items.iterator();
                    Iterator<AppInitItem> it1 = group.items.iterator();
                    while (it0.hasNext()) {
                        AppInitItem item0 = it0.next();
                        AppInitItem item1 = it1.next();
                        if (!item0.equals(item1)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }

            } else if (group.hasItems()) {
                return false;
            }

            return true;
        }
        return false;
    }

    public AppInitGroup clone() {
        AppInitGroup group = new AppInitGroup();
        group.title = title;
        if (null != items) {
            group.items = new LinkedList<>();
            for (AppInitItem item : items) {
                group.items.add(item.clone());
            }
        }
        return group;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String HR = Strings.dup('-', 40);
        if (this.hasTitle()) {
            sb.append("# ").append(HR).append('\n');
            sb.append("# ").append('\n');
            sb.append("# ").append(title).append('\n');
            sb.append("# ").append('\n');
            sb.append("# ").append(HR).append('\n');
        }

        if (this.hasItems()) {
            for (AppInitItem item : items) {
                sb.append(item.toString()).append('\n');
                sb.append("# ").append(HR).append('\n');
            }
        }

        return sb.toString();
    }

    public boolean hasTitle() {
        return !Strings.isBlank(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean hasItems() {
        return null != items && !items.isEmpty();
    }

    public void addItem(AppInitItem item) {
        if (null == items) {
            this.items = new LinkedList<>();
        }
        this.items.add(item);
    }

    public List<AppInitItem> getItems() {
        return items;
    }

    public void setItems(List<AppInitItem> items) {
        this.items = items;
    }

}
