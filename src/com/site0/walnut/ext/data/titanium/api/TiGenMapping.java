package com.site0.walnut.ext.data.titanium.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.lang.born.Borning;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.titanium.WnI18nService;
import com.site0.walnut.ext.data.titanium.impl.TiGenExportMappingByForm;
import com.site0.walnut.ext.data.titanium.impl.TiGenExportMappingByTable;
import com.site0.walnut.ext.data.titanium.impl.TiGenImportMappingByForm;
import com.site0.walnut.ext.data.titanium.impl.TiGenImportMappingByTable;
import com.site0.walnut.ext.data.titanium.util.TiDict;
import com.site0.walnut.ext.data.titanium.util.TiDictFactory;
import com.site0.walnut.util.Ws;

public abstract class TiGenMapping {

    /**
     * 子类决定将表单/表格字段加入映射的具体方式
     * 
     * @param field
     *            表单或者表格字段
     * @param forceFieldType
     *            强制修改字段类型（在输出映射时，如果是到excel的映射，需要这个）. 否则 cmd_sheed 只会把数组变JSON
     * 
     */
    protected abstract void joinField(NutMap field, String forceFieldType);

    private static final Map<String, Borning<? extends TiGenMapping>> instances = new HashMap<>();

    static {
        instances.put("export_table", Mirror.me(TiGenExportMappingByTable.class).getBorning());
        instances.put("export_form", Mirror.me(TiGenExportMappingByForm.class).getBorning());
        instances.put("import_table", Mirror.me(TiGenImportMappingByTable.class).getBorning());
        instances.put("import_form", Mirror.me(TiGenImportMappingByForm.class).getBorning());
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

    private WnI18nService i18ns;

    private String lang;

    private String forceFieldType;

    public TiGenMapping() {
        mapping = new NutMap();
    }

    public NutMap genMapping(List<NutMap> fields) {
        String fft = Ws.upperFirst(forceFieldType);
        for (NutMap field : fields) {
            joinField(field, fft);
        }
        return mapping;
    }

    public NutMap getMapping() {
        return mapping;
    }

    public void putFieldMapping(String key, Object val) {
        mapping.put(key, val);
    }

    public String traslateText(String text) {
        String re = text;
        if (null != i18ns) {
            String s = i18ns.getText(lang, text);
            if (null != s) {
                re = s;
            }
        }
        return re;
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

    public String getForceFieldType() {
        return forceFieldType;
    }

    public void setForceFieldType(String forceFieldType) {
        this.forceFieldType = forceFieldType;
    }

    public WnI18nService getI18ns() {
        return i18ns;
    }

    public void setI18ns(WnI18nService i18ns) {
        this.i18ns = i18ns;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
