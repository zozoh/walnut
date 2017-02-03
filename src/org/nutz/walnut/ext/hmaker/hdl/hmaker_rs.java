package org.nutz.walnut.ext.hmaker.hdl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(regex = "^(obj)$", value = "cqn")
public class hmaker_rs implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到站点主目录
        WnObj oSiteHome = hc.oRefer;
        String siteHomePath = oSiteHome.path();

        // 得到搜索路径列表
        List<WnObj> tops = new LinkedList<WnObj>();

        if (hc.params.has("path")) {
            String[] phs = Strings.splitIgnoreBlank(hc.params.getString("path"));
            for (String ph : phs) {
                WnObj oDir = sys.io.check(oSiteHome, ph);
                tops.add(oDir);
            }
        }
        // 整站搜索
        else {
            tops.add(oSiteHome);
        }

        // 准备过滤器
        String m_str = hc.params.get("match");
        boolean isNot;
        Pattern regex;
        if (!Strings.isBlank(m_str)) {
            isNot = m_str.startsWith("!");
            if (isNot)
                m_str = Strings.trim(m_str.substring(1));
            regex = Pattern.compile(m_str);
        } else {
            regex = null;
            isNot = false;
        }

        // 在整个搜索路径下寻找结果
        List<WnObj> list = new LinkedList<WnObj>();
        for (WnObj oDir : tops) {
            sys.io.walk(oDir, new Callback<WnObj>() {
                public void invoke(WnObj o) {
                    // 得到相对路径
                    String rph = Disks.getRelativePath(siteHomePath, o.path());

                    // 看看是否匹配
                    if (null != regex && !(regex.matcher(rph).find() ^ isNot)) {
                        return;
                    }

                    // 计入结果
                    o.setv("rph", rph);
                    list.add(o);
                }
            }, WalkMode.LEAF_ONLY);
        }

        // 输出结果
        if (hc.params.has("key")) {
            String[] keys = Strings.splitIgnoreBlank(hc.params.get("key"));

            // 如果只有一个字段，默认会输出字符串，除非强制为对象
            if (keys.length == 1 && !hc.params.is("obj")) {
                List<Object> outs = new ArrayList<>(list.size());
                String key = keys[0];
                for (WnObj o : list) {
                    Object v = o.get(key);
                    outs.add(v);
                }
                sys.out.print(Json.toJson(outs, hc.jfmt));
            }
            // 过滤字段
            else {
                List<NutMap> outs = new ArrayList<>(list.size());
                for (WnObj o : list) {
                    NutMap map = NutMap.WRAP(o).pick(keys);
                    outs.add(map);
                }
                sys.out.print(Json.toJson(outs, hc.jfmt));
            }
        }
        // 默认的输出全部的 JSON
        else {
            sys.out.print(Json.toJson(list, hc.jfmt));
        }

    }

}
