package com.site0.walnut.ext.data.thing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

/**
 * 服务器端对 thing.json 的解析结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ThingConf {

    /**
     * 当数据创建时，固有的初始字段值
     */
    private NutMap initMeta;

    /**
     * 这里根据条件，自动设置动态的初始值
     */
    private ThTestMeta[] testInitMetas;

    private ThingUniqueKey[] uniqueKeys;

    private ThingField[] fields;

    private boolean updateKeepType;

    // zozoh: 先去掉这两个奇怪的键名，用原生的，看看会发生什么
    // @JsonField("lnKeys")

    /**
     * 可以是 Map<String, ThingLinkKey> 或者 List<Map<String, ThingLinkKey>>
     */
    private Object linkKeys;

    private String[] onCreated;

    private String[] onBeforeUpdate;

    private String[] onUpdated;

    private String[] onBeforeDelete;

    private String[] onDeleted;

    public boolean hasInitMeta() {
        return null != initMeta && !initMeta.isEmpty();
    }

    public NutMap getInitMeta() {
        return initMeta;
    }

    public void setInitMeta(NutMap initMeta) {
        this.initMeta = initMeta;
    }

    public boolean hasOnBeforeUpdate() {
        return null != onBeforeUpdate && onBeforeUpdate.length > 0;
    }

    public boolean hasOnUpdated() {
        return null != onUpdated && onUpdated.length > 0;
    }

    public boolean hasTestInitMetas() {
        return null != testInitMetas && testInitMetas.length > 0;
    }

    public ThTestMeta[] getTestInitMetas() {
        return testInitMetas;
    }

    public void setTestInitMetas(ThTestMeta[] testMetas) {
        this.testInitMetas = testMetas;
    }

    public boolean hasUniqueKeys() {
        return null != uniqueKeys && uniqueKeys.length > 0;
    }

    public Map<String, ThingUniqueKey> getUniqueKeyNameMap() {
        Map<String, ThingUniqueKey> map = new HashMap<>();
        if (null != uniqueKeys) {
            for (ThingUniqueKey uk : uniqueKeys) {
                String[] ukNames = uk.getName();
                if (null != ukNames) {
                    for (String ukName : ukNames) {
                        map.put(ukName, uk);
                    }
                }
            }
        }
        return map;
    }

    public ThingUniqueKey[] getUniqueKeys() {
        return uniqueKeys;
    }

    public void setUniqueKeys(ThingUniqueKey[] uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    public boolean hasLinkKeys() {
        return null != linkKeys && this.getLinkKeyList().size() > 0;
    }

    public Object getLinkKeys() {
        return linkKeys;
    }

    public void setLinkKeys(Object linkKeys) {
        this.linkKeys = linkKeys;
    }

    private List<Map<String, ThingLinkKey>> _lnk_list;

    public List<Map<String, ThingLinkKey>> getLinkKeyList() {
        if (null == _lnk_list) {
            if (null != linkKeys) {
                // 自己就是一个 Map
                if (linkKeys instanceof Map<?, ?>) {
                    _lnk_list = new ArrayList<>(1);
                    Map<String, ThingLinkKey> lkMap = __eval_linkKeyMap(linkKeys);
                    _lnk_list.add(lkMap);
                }
                // 那就是列表咯
                else if (linkKeys instanceof Collection<?>) {
                    Collection<?> col = (Collection<?>) linkKeys;
                    _lnk_list = new ArrayList<>(col.size());
                    for (Object o : col) {
                        Map<String, ThingLinkKey> lkMap = __eval_linkKeyMap(o);
                        _lnk_list.add(lkMap);
                    }
                }
                // 不支持
                else {
                    throw Wlang.impossible();
                }
            }
        }
        return _lnk_list;
    }

    public int getLinkKeyMetaCapSize() {
        int c = 0;
        if (null != this.getLinkKeyList()) {
            for (Map<String, ThingLinkKey> map : this.getLinkKeyList()) {
                c += map.size();
            }
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    public Map<String, ThingLinkKey> __eval_linkKeyMap(Object obj) {
        Map<String, Object> inMap = (Map<String, Object>) obj;
        Map<String, ThingLinkKey> reMap = new HashMap<>();
        for (Map.Entry<String, Object> en : inMap.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if (null != val && val instanceof Map<?, ?>) {
                ThingLinkKey lk = new ThingLinkKey((Map<String, Object>) val);
                reMap.put(key, lk);
            }
        }
        return reMap;
    }

    public String[] getOnCreated() {
        return onCreated;
    }

    public void setOnCreated(String[] onCreate) {
        this.onCreated = onCreate;
    }

    public String[] getOnBeforeUpdate() {
        return onBeforeUpdate;
    }

    public void setOnBeforeUpdate(String[] onBeforeUpdate) {
        this.onBeforeUpdate = onBeforeUpdate;
    }

    public String[] getOnUpdated() {
        return onUpdated;
    }

    public void setOnUpdated(String[] onUpdated) {
        this.onUpdated = onUpdated;
    }

    public String[] getOnBeforeDelete() {
        return onBeforeDelete;
    }

    public void setOnBeforeDelete(String[] onBeforeDelete) {
        this.onBeforeDelete = onBeforeDelete;
    }

    public String[] getOnDeleted() {
        return onDeleted;
    }

    public void setOnDeleted(String[] onDeleted) {
        this.onDeleted = onDeleted;
    }

    public ThingField[] getFields() {
        return fields;
    }

    public void setFields(ThingField[] fields) {
        this.fields = fields;
    }

    public boolean isUpdateKeepType() {
        return updateKeepType;
    }

    public void setUpdateKeepType(boolean updateKeepType) {
        this.updateKeepType = updateKeepType;
    }

    public void validate(NutBean meta, boolean ignoreNoExists) {
        if (null != fields) {
            for (ThingField fld : fields) {
                if (ignoreNoExists && !meta.containsKey(fld.getKey()))
                    continue;
                fld.validate(meta);
            }
        }
    }

}
