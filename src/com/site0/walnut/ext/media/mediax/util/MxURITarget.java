package com.site0.walnut.ext.media.mediax.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.site0.walnut.util.Wlang;

public class MxURITarget {

    private MxURI[] crawl;

    private MxURI[] fetch;

    private MxURI[] post;

    private MxURI[] download;

    // 用来缓存的
    private Map<String, MxURI> __crawl;
    private Map<String, MxURI> __fetch;
    private Map<String, MxURI> __post;
    private Map<String, MxURI> __download;

    public String getCrawlPath(String key, String[] args) {
        return getPath("crawl", key, args);
    }

    public String getFetchPath(String key, String[] args) {
        return getPath("fetch", key, args);
    }

    public String getPostPath(String key, String[] args) {
        return getPath("post", key, args);
    }

    public String getDownloadPath(String key, String[] args) {
        return getPath("download", key, args);
    }

    private String __get_path(Map<String, MxURI> map, String key, String[] args) {
        MxURI uri = map.get(key);
        if (null == uri)
            return null;
        return uri.toFullPath(true, args);
    }

    public String getPath(String actionName, String key, String[] args) {
        Map<String, MxURI> map = __map(actionName);
        return this.__get_path(map, key, args);
    }

    private Map<String, MxURI> __map(String actionName) {
        if ("crawl".equals(actionName)) {
            if (null == this.__crawl)
                this.__crawl = this.__build_map(crawl);
            return __crawl;
        }
        if ("fetch".equals(actionName)) {
            if (null == this.__fetch)
                this.__fetch = this.__build_map(fetch);
            return __fetch;
        }
        if ("post".equals(actionName)) {
            if (null == this.__post)
                this.__post = this.__build_map(post);
            return __post;
        }
        if ("download".equals(actionName)) {
            if (null == this.__download)
                this.__download = this.__build_map(download);
            return __download;
        }
        throw Wlang.makeThrow("map name invalid ", actionName);
    }

    private Map<String, MxURI> __build_map(MxURI[] list) {
        Map<String, MxURI> map = new LinkedHashMap<>();
        if (null != list && list.length > 0)
            for (MxURI uri : list) {
                map.put(uri.getName(), uri);
            }
        return map;
    }

    public String toString() {
        return dump(null, true);
    }

    public String dump(String actionName, boolean showTmpl) {
        if (null == actionName) {
            String re = __dump_map("crawl", showTmpl);
            re += __dump_map("fetch", showTmpl);
            re += __dump_map("post", showTmpl);
            re += __dump_map("download", showTmpl);
            return re;
        }
        return this.__dump_map(actionName, showTmpl);
    }

    private String __dump_map(String actionName, boolean showTmpl) {
        Map<String, MxURI> map = __map(actionName);
        String re = actionName + ":\n";
        if (map.isEmpty()) {
            re += "    --\n";
        } else {
            for (MxURI uri : map.values()) {
                re += "    " + (showTmpl ? uri.toString() : uri.toExample()) + "\n";
            }
        }
        return re;
    }

}
