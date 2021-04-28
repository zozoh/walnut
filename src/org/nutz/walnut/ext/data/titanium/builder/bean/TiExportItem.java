package org.nutz.walnut.ext.data.titanium.builder.bean;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Ws;

public class TiExportItem {

    private String name;

    private String code;

    private WnObj obj;

    private int importCount;

    public TiExportItem(String name, WnObj obj) {
        this.name = name;
        this.obj = obj;
    }

    public TiExportItem(String code) {
        this(null, code);
    }

    public TiExportItem(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasCode() {
        return !Ws.isBlank(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean hasObj() {
        return null != obj;
    }

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
    }

    public int getImportCount() {
        return importCount;
    }

    public void setImportCount(int importCount) {
        this.importCount = importCount;
    }

}
