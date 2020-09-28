package org.nutz.walnut.ext.titanium.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.nutz.json.JsonIgnore;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.match.AutoStrMatch;

public class TiViewMapping {

    static class MappingItem {
        WnMatch test;
        String value;
    }

    private Map<String, String> paths;

    private Map<String, String> types;

    private Map<String, String> mimes;

    private Map<String, String> races;

    @JsonIgnore
    private List<MappingItem> _match_paths;

    @JsonIgnore
    private List<MappingItem> _match_types;

    @JsonIgnore
    private List<MappingItem> _match_mimes;

    public String match(WnObj o) {
        // 根据路径，需要格式化为标准路径形式
        if (null != this._match_paths) {
            String path = o.getFormedPath(true);
            for (MappingItem it : this._match_paths) {
                if (it.test.match(path)) {
                    return it.value;
                }
            }
        }

        // 根据类型（精确）
        String type = o.type();
        if (null != this._match_types && null != type) {
            for (MappingItem it : this._match_types) {
                if (it.test.match(type)) {
                    return it.value;
                }
            }
        }

        // 根据MIME（精确）
        if (o.hasMime() && o.isFILE() && null != this._match_mimes) {
            String mime = o.mime();
            for (MappingItem it : this._match_mimes) {
                if (it.test.match(mime)) {
                    return it.value;
                }
            }
        }

        // 根据 RACE
        String race = o.race().name();
        if (null != races) {
            return races.get(race);
        }

        // 神马玩意？
        return null;
    }

    private List<MappingItem> evalMap(Map<String, String> map) {
        if (null == map)
            return null;
        List<MappingItem> list = new ArrayList<>(map.size());
        for (Map.Entry<String, String> en : map.entrySet()) {
            String key = en.getKey();
            String val = en.getValue();
            MappingItem it = new MappingItem();
            it.test = new AutoStrMatch(key);
            it.value = val;
            list.add(it);
        }
        return list;
    }

    public Map<String, String> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, String> paths) {
        this.paths = paths;
        this._match_paths = this.evalMap(paths);
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
        this._match_types = this.evalMap(types);
    }

    public Map<String, String> getMimes() {
        return mimes;
    }

    public void setMimes(Map<String, String> mimes) {
        this.mimes = mimes;
        this._match_mimes = this.evalMap(mimes);
    }

    public Map<String, String> getRaces() {
        return races;
    }

    public void setRaces(Map<String, String> races) {
        this.races = races;
    }

}
