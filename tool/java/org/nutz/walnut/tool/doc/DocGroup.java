package org.nutz.walnut.tool.doc;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DocGroup extends DocItem {

    private static final Map<String, String> tmap = new HashMap<>();

    static {
        tmap.put("about", "关于");
        tmap.put("core-l0", "核心概念");
        tmap.put("core-l1", "延申概念");
        tmap.put("core-l2", "扩展概念");
        tmap.put("func-l0", "内置功能");
        tmap.put("func-l1", "扩展功能");
        tmap.put("thing-l0", "通用数据集");
        tmap.put("web-l0", "Web核心");
        tmap.put("web-l1", "Web扩展");
    }

    public DocGroup(File dHome, File file) {
        super(dHome, file);
    }

    private List<DocItem> items;

    @Override
    public String getDisplayTitle() {
        String title = super.getDisplayTitle();
        if (tmap.containsKey(title))
            return tmap.get(title);
        return title;
    }

    public void joinString(StringBuilder sb) {
        sb.append("# ").append(this.getDisplayTitle()).append('\n');
        if (null != items) {
            sb.append('\n');
            for (DocItem it : items) {
                sb.append("- ");
                it.joinString(sb);
                sb.append('\n');
            }
        }
        sb.append('\n');
    }

    public void joinRefers(StringBuilder sb) {
        if (null != items) {
            for (DocItem it : items) {
                it.joinRefers(sb);
            }
        }
    }

    public DocGroup sortItems() {
        Collections.sort(this.items);
        return this;
    }

    public void addItem(File file) {
        if (null == items) {
            items = new LinkedList<>();
        }
        DocItem it = new DocItem(this.dHome, file);
        items.add(it);
    }

    public List<DocItem> getItems() {
        return items;
    }

    public void setItems(List<DocItem> items) {
        this.items = items;
    }

}
