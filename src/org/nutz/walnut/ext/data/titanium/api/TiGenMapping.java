package org.nutz.walnut.ext.data.titanium.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.lang.born.Borning;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.data.titanium.impl.TiGenExportMappingAsForm;
import org.nutz.walnut.ext.data.titanium.impl.TiGenExportMappingAsTable;
import org.nutz.walnut.ext.data.titanium.impl.TiGenImportMappingAsForm;
import org.nutz.walnut.ext.data.titanium.impl.TiGenImportMappingAsTable;
import org.nutz.walnut.ext.data.titanium.util.TiDict;
import org.nutz.walnut.ext.data.titanium.util.TiDictFactory;
import org.nutz.walnut.util.Ws;

public abstract class TiGenMapping {

    /**
     * 子类决定将表单/表格字段加入映射的具体方式
     * 
     * @param field
     *            表单或者表格字段
     */
    protected abstract void joinField(NutMap field);

    private static final Map<String, Borning<? extends TiGenMapping>> instances = new HashMap<>();

    static {
        instances.put("export_table", Mirror.me(TiGenExportMappingAsTable.class).getBorning());
        instances.put("export_form", Mirror.me(TiGenExportMappingAsForm.class).getBorning());
        instances.put("import_table", Mirror.me(TiGenImportMappingAsTable.class).getBorning());
        instances.put("import_form", Mirror.me(TiGenImportMappingAsForm.class).getBorning());
    }

    public static TiGenMapping getInstance(String key) {
        Borning<? extends TiGenMapping> born = instances.get(key);
        TiGenMapping gm = born.born();
        return gm;
    }

    private NutMap mapping;

    private String[] whiteList;

    private String[] blackList;

    private Map<String, Boolean> whites;

    private Map<String, Boolean> blacks;

    private TiDictFactory dicts;

    public TiGenMapping() {
        mapping = new NutMap();
    }

    public NutMap genMapping(List<NutMap> fields) {
        for (NutMap field : fields) {
            joinField(field);
        }
        return mapping;
    }

    public NutMap getMapping() {
        return mapping;
    }

    public void putFieldMapping(String key, Object val) {
        mapping.put(key, val);
    }

    private Map<String, Boolean> __build_list_map(String[] list) {
        Map<String, Boolean> a;
        if (null == list || list.length == 0) {
            a = null;
        } else {
            a = new HashMap<>();
            for (String li : list) {
                a.put(li, true);
            }
        }
        return a;
    }

    protected boolean isDefault(String name, boolean dftRe) {
        if (null != whites && !whites.containsKey(name)) {
            return false;
        }
        if (null != blacks && blacks.containsKey(name)) {
            return false;
        }
        return dftRe;
    }

    public String[] getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(String[] list) {
        this.whiteList = list;
        this.whites = __build_list_map(list);
    }

    public String[] getBlackList() {
        return blackList;
    }

    public void setBlackList(String[] list) {
        this.blackList = list;
        this.blacks = __build_list_map(list);
    }

    public TiDictFactory getDicts() {
        return dicts;
    }

    public void setDicts(TiDictFactory dicts) {
        this.dicts = dicts;
    }

    public void setDicts(String dicts) {
        if (Ws.isBlank(dicts))
            this.dicts = null;
        else
            this.dicts = new TiDictFactory(dicts);
    }

    public boolean hasDicts() {
        return null != this.dicts;
    }

    public boolean hasDict(String name) {
        if (null == dicts)
            return false;
        return dicts.hasDict(name);
    }

    public TiDict getDict(String name) {
        if (null == dicts)
            return null;
        return dicts.getDict(name);
    }

}
