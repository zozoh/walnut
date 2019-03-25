package org.nutz.walnut.ext.titanium.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.json.JsonIgnore;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class TiViewMapping {

    static class RegExpMatchView {
        Pattern regex;
        TiView view;

        RegExpMatchView(String regex, TiView view) {
            this.regex = Pattern.compile(regex);
            this.view = view;
        }
    }

    private Map<String, TiView> paths;

    private Map<String, TiView> types;

    private Map<String, TiView> mimes;

    private Map<String, TiView> races;

    @JsonIgnore
    private List<RegExpMatchView> __type_views;

    public TiViewMapping() {
        this.__type_views = new LinkedList<>();
    }

    public void init(WnIo io, WnObj oMapping) {
        this.__check_view_in_map(io, oMapping, paths);
        this.__check_view_in_map(io, oMapping, types);
        this.__check_view_in_map(io, oMapping, mimes);
        this.__check_view_in_map(io, oMapping, races);
        // 没必要检查 __type_views，因为它只是维护到 mimes 里面视图的一份引用
    }

    private void __check_view_in_map(WnIo io, WnObj oMapping, Map<String, TiView> map) {
        if (null != map && !map.isEmpty()) {
            for (TiView view : map.values()) {
                this.__check_view(io, oMapping, view);
            }
        }
    }

    private void __check_view(WnIo io, WnObj oMapping, TiView view) {
        // 如果是引用菜单，需要再加载一次
        Object actions = view.getActions();
        if (null != actions && (actions instanceof String)) {
            String actionsPath = actions.toString();
            WnObj actionObj = io.check(oMapping, actionsPath);
            String json = io.readText(actionObj);
            List<NutMap> actionList = Json.fromJsonAsList(NutMap.class, json);
            view.setActions(actionList);
        }
    }

    public TiView match(WnObj o) {
        TiView view = null;
        // 根据路径，需要格式化为标准路径形式
        String path = o.getFormedPath(true);
        if (null != paths) {
            view = paths.get(path);
            if (null != view)
                return view;
        }

        // 根据类型（精确）
        String type = o.type();
        if (null != types) {
            view = types.get(type);
            if (null != view)
                return view;
        }

        // 根据类型（正则）
        for (RegExpMatchView rmv : this.__type_views) {
            if (rmv.regex.matcher(type).find()) {
                return rmv.view;
            }
        }

        // 根据MIME（精确）
        if (o.hasMime()) {
            String mime = o.mime();
            if (null != mimes) {
                view = mimes.get(mime);
            }
            // 根据MIME（组）
            if (null == view) {
                String mimeGroup = Wn.Mime.getGroupName(mime, "");
                view = mimes.get(mimeGroup);
            }
            if (null != view)
                return view;
        }

        // 根据 RACE
        String race = o.race().name();
        if (null != races) {
            return races.get(race);
        }

        // 神马玩意？
        return null;
    }

    public Map<String, TiView> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, TiView> paths) {
        this.paths = paths;
    }

    public Map<String, TiView> getTypes() {
        return types;
    }

    public void setTypes(Map<String, TiView> types) {
        this.types = types;
        // 收集正则表达式匹配的键
        __type_views.clear();
        for (Map.Entry<String, TiView> en : types.entrySet()) {
            String key = en.getKey();
            TiView val = en.getValue();
            if (key.startsWith("^")) {
                RegExpMatchView rmv = new RegExpMatchView(key, val);
                __type_views.add(rmv);
            }
        }
    }

    public Map<String, TiView> getMimes() {
        return mimes;
    }

    public void setMimes(Map<String, TiView> mimes) {
        this.mimes = mimes;
    }

    public Map<String, TiView> getRaces() {
        return races;
    }

    public void setRaces(Map<String, TiView> races) {
        this.races = races;
    }

}
