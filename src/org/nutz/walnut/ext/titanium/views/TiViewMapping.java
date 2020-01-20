package org.nutz.walnut.ext.titanium.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.json.JsonIgnore;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

public class TiViewMapping {

    static class RegExpMatchView {
        Pattern regex;
        String viewName;

        RegExpMatchView(String regex, String view) {
            this.regex = Pattern.compile(regex);
            this.viewName = view;
        }

        public String toString() {
            return String.format("[%s]: %s", viewName, regex);
        }
    }

    static class ArrayMatchView {
        String[] list;
        String viewName;

        ArrayMatchView(String path, String view) {
            this.list = Strings.splitIgnoreBlank(path, "[/\\\\]");
            this.viewName = view;
        }

        boolean isMatch(String[] paths) {
            if (list.length != paths.length) {
                return false;
            }
            for (int i = 0; i < list.length; i++) {
                String li = list[i];
                String pi = paths[i];
                if (null == li || null == pi) {
                    return false;
                }
                // 通配
                if ("*".equals(li)) {
                    continue;
                }
                // 精确匹配
                if (!li.equals(pi)) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            return String.format("[%s]: %s", viewName, Strings.join("/", list));
        }
    }

    private Map<String, String> paths;

    private List<ArrayMatchView> __wildcard_paths;

    private Map<String, String> types;

    private Map<String, String> mimes;

    private Map<String, String> races;

    @JsonIgnore
    private List<RegExpMatchView> __type_views;

    public TiViewMapping() {
        this.__wildcard_paths = new LinkedList<>();
        this.__type_views = new LinkedList<>();
    }

    public String match(WnObj o) {
        String viewName = null;
        // 根据路径，需要格式化为标准路径形式
        String path = o.getFormedPath(true);
        if (null != paths) {
            viewName = paths.get(path);
            if (null != viewName)
                return viewName;
        }

        // 通配符路径匹配
        if (null != this.__wildcard_paths && !this.__wildcard_paths.isEmpty()) {
            String[] list = Strings.splitIgnoreBlank(path, "[/\\\\]");
            for (ArrayMatchView amv : this.__wildcard_paths) {
                if (amv.isMatch(list)) {
                    return amv.viewName;
                }
            }
        }

        // 根据类型（精确）
        String type = o.type();
        if (null != types) {
            viewName = types.get(type);
            if (null != viewName)
                return viewName;
        }

        // 根据类型（正则）
        for (RegExpMatchView rmv : this.__type_views) {
            if (rmv.regex.matcher(type).find()) {
                return rmv.viewName;
            }
        }

        // 根据MIME（精确）
        if (o.hasMime()) {
            String mime = o.mime();
            if (null != mimes) {
                viewName = mimes.get(mime);
            }
            // 根据MIME（组）
            if (null == viewName) {
                String mimeGroup = Wn.Mime.getGroupName(mime, "");
                viewName = mimes.get(mimeGroup);
            }
            if (null != viewName)
                return viewName;
        }

        // 根据 RACE
        String race = o.race().name();
        if (null != races) {
            return races.get(race);
        }

        // 神马玩意？
        return null;
    }

    public Map<String, String> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, String> paths) {
        this.paths = new HashMap<>();
        this.__wildcard_paths.clear();

        for (Map.Entry<String, String> en : paths.entrySet()) {
            String key = en.getKey();
            String val = en.getValue();
            // 通配符
            if (key.contains("*")) {
                this.__wildcard_paths.add(new ArrayMatchView(key, val));
            }
            // 通用路径
            else {
                this.paths.put(key, val);
            }
        }
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
        __type_views.clear();

        for (Map.Entry<String, String> en : types.entrySet()) {
            String regex = en.getKey();
            String viewName = en.getValue();
            if (regex.startsWith("^")) {
                RegExpMatchView rmv = new RegExpMatchView(regex, viewName);
                __type_views.add(rmv);
            }
        }
    }

    public Map<String, String> getMimes() {
        return mimes;
    }

    public void setMimes(Map<String, String> mimes) {
        this.mimes = mimes;
    }

    public Map<String, String> getRaces() {
        return races;
    }

    public void setRaces(Map<String, String> races) {
        this.races = races;
    }

}
