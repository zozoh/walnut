package org.nutz.walnut.ext.dsync.bean;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.archive.WnArchiveSummary;

public class WnDataSyncTree {

    private String name;

    private WnObj treeObj;

    private WnObj metaObj;

    private long dataSyncTime;

    private List<WnDataSyncItem> items;

    public WnDataSyncTree() {
        this.items = new LinkedList<>();
    }

    public WnArchiveSummary summarize() {
        WnArchiveSummary stc = new WnArchiveSummary();
        for (WnDataSyncItem item : items) {
            stc.items++;
            if (item.isDir()) {
                stc.dir++;
            } else {
                stc.file++;
                stc.size += item.getLen();
            }
        }
        return stc;
    }

    public void joinBreif(StringBuilder sb) {
        sb.append("TREE[").append(name).append("]\n");
        String ds = Ws.formatAms(dataSyncTime);
        sb.append("sync_time: ").append(ds).append("\n");

        if (null != treeObj) {
            sb.append("tree_obj: ");
            sb.append(Ws.sizeText(treeObj.len()));
            sb.append("\n");
        }

        if (null != metaObj) {
            sb.append("meta_obj: ");
            sb.append(Ws.sizeText(metaObj.len()));
            sb.append("\n");
        }

        // Get items count
        WnArchiveSummary sum = this.summarize();
        sum.joinString(sb);
    }

    public String toBreif() {
        StringBuilder sb = new StringBuilder();
        this.joinBreif(sb);
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinBreif(sb);
        sb.append("\n\n");
        this.joinContentString(sb);
        return sb.toString();
    }

    public String toContentString() {
        StringBuilder sb = new StringBuilder();
        this.joinContentString(sb);
        return sb.toString();
    }

    public void joinContentString(StringBuilder sb) {
        if (null != items)
            for (WnDataSyncItem item : items) {
                sb.append(item.toString()).append("\n");
            }
    }

    public String toMetaString(JsonFormat jfmt) {
        NutMap map = new NutMap();
        if (null == jfmt) {
            jfmt = JsonFormat.compact().setQuoteName(true);
        }
        for (WnDataSyncItem item : items) {
            map.put(item.getBeanSha1(), item.getBean());
            map.put(item.getMetaSha1(), item.getMeta());
        }
        return Json.toJson(map, jfmt);
    }

    public void load(WnIo io, WnObj oTree) {
        this.treeObj = oTree;
        this.dataSyncTime = oTree.getLong("dsync_t");
        String input = io.readText(oTree);
        this.parse(input);
    }

    public void parse(String input) {
        String[] lines = Ws.splitIgnoreBlank(input, "\r?\n");
        if (null != lines) {
            for (String line : lines) {
                if (null != line && !line.startsWith("#")) {
                    this.addItem(line);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setName(String confName, String key) {
        this.name = confName + "-" + key;
    }

    public WnObj getTreeObj() {
        return treeObj;
    }

    public void setTreeObj(WnObj obj) {
        this.treeObj = obj;
    }

    public WnObj getMetaObj() {
        return metaObj;
    }

    public void setMetaObj(WnObj metaObj) {
        this.metaObj = metaObj;
    }

    public boolean isMatchSyncTime(WnObj oDir) {
        long st = oDir.syncTime();
        return st == this.dataSyncTime;
    }

    public long getDataSyncTime() {
        return dataSyncTime;
    }

    public void setDataSyncTime(long dataSyncTime) {
        this.dataSyncTime = dataSyncTime;
    }

    public boolean hasItems() {
        return null == items || !items.isEmpty();
    }

    public void addItem(String input) {
        WnDataSyncItem item = new WnDataSyncItem(input);
        items.add(item);
    }

    public void addItem(WnDataSyncItem item) {
        items.add(item);
    }

    public List<WnDataSyncItem> getItems() {
        return items;
    }

    public void clearItems() {
        this.items.clear();
    }

}
