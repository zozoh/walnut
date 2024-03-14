package com.site0.walnut.ext.sys.dsync.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wpath;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.archive.WnArchiveSummary;

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
        // 记录索引树对应的文件夹同步时间
        map.put("dsync_t", this.dataSyncTime);

        // 记录每个元数据的具体细节
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

    /**
     * 建立一个 ID-Path 映射表。
     * <p>
     * 
     * @param map
     *            要加入的 ID-Path 映射表。
     *            <p>
     *            其中，Id 为 Bean里面的 ID, Path 为 "~" 开头的路径，<br>
     *            即，在 item 声明的路径。这样以便转移到不同的域时便于获取对于路径。
     */
    public void joinIdPathMap(Map<String, String> map) {
        if (this.hasItems()) {
            for (WnDataSyncItem it : this.items) {
                NutBean bean = it.getBean();
                if (null != bean) {
                    String id = bean.getString("id");
                    String path = it.getPath();
                    map.put(id, path);
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

    public String getTreeName() {
        if (null != treeObj) {
            String nm = treeObj.name();
            return Wpath.getMajorName(nm);
        }
        return null;
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
