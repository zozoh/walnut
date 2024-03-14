package com.site0.walnut.ext.media.mediax.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;

/**
 * 存储了一个 URI 模板的全部必要信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class MxURI {

    /**
     * 譬如 "/provinces"
     */
    private String path;

    /**
     * 快捷名，譬如 "最新备案"
     */
    private String name;

    /**
     * 支持的 QueryString 参数名称表
     */
    private String[] params;

    /**
     * 对于参数例子值，主要是 toQuickPath 用
     */
    private String[] examples;

    public String toFullPath(boolean encode, String... args) {
        String re = path;
        // 准备循环
        int len = 0;
        if (params != null && args != null)
            len = Math.min(params.length, args.length);
        // 收集参数
        ArrayList<String> qs = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            String key = params[i];
            String val = args[i];
            if (!Strings.isBlank(val)) {
                if (encode)
                    try {
                        val = URLEncoder.encode(val, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        throw Er.wrap(e);
                    }
                qs.add(key + "=" + val);
            }
        }
        // 来吧，开始拼装
        if (qs.size() > 0) {
            // 确保拼装的参数头
            if (path.lastIndexOf('?') < 0) {
                re += "?";
            } else {
                re += "&";
            }
            // 拼装参数
            re += Strings.join("&", qs);
        }
        return re;
    }

    public String toQuickPath() {
        List<String> qs = new ArrayList<>();
        for (String key : params) {
            qs.add("${" + key + "}");
        }
        if (qs.size() > 0) {
            return name + "/" + Strings.join("/", qs);
        }
        return name;
    }

    public String toExample() {
        String re = name;
        if (null != examples && examples.length > 0) {
            re += "/" + Strings.join("/", examples);
        }
        re += " : ";
        re += this.toFullPath(false, examples);
        return re;
    }

    public String toString() {
        return this.toQuickPath() + " : " + this.toExample();
    }

    // /**
    // * @return 一个用来填充参数的 Map
    // */
    // public NutMap genParamsMap() {
    // NutMap map = new NutMap();
    // for (String p : paramNames)
    // map.put(p, null);
    // return map;
    // }

    public MxURI setPath(String path) {
        this.path = path;
        return this;
    }

    public MxURI setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public MxURI setParams(String... params) {
        this.params = params;
        return this;
    }

}
