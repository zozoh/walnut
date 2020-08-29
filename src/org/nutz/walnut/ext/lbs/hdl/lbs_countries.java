package org.nutz.walnut.ext.lbs.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.lbs.cmd_lbs;
import org.nutz.walnut.ext.lbs.bean.LbsCountry;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.ajax.Ajax;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class lbs_countries implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String lang = hc.params.getString("lang", "zh_cn");
        String map = hc.params.getString("map");

        // 首先过去国家列表
        List<LbsCountry> list = null;
        if (hc.params.vals.length > 0) {
            list = cmd_lbs.getCountryObjListBy(hc.params.vals);
        }
        // 否则就是全部国家
        else {
            list = cmd_lbs.getCountryObjList();
        }

        Object re;

        // 映射为对象
        if ("obj".equals(map)) {
            re = getCountryObjMap(list, lang);
        }
        // 映射为名称
        else if ("name".equals(map)) {
            re = getCountryNameMap(list, lang);
        }
        // 直接就是列表
        else {
            re = getCountries(list, lang);
        }

        if (hc.params.is("ajax")) {
            re = Ajax.ok().setData(re);
        }

        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

    /**
     * 获取国家名值对映射
     * 
     * <pre>
     * {"CN":{key:"CN", name:"中国"}}
     * </pre>
     * 
     * @param list
     *            国家对象列表
     * @param lang
     *            语言
     * @return 一个对家名值对映射
     */
    private static NutMap getCountryObjMap(List<LbsCountry> list, String lang) {
        NutMap re = new NutMap();
        for (LbsCountry lc : list) {
            String key = lc.getKey();
            Object val = lc.toMap(lang);
            re.put(key, val);
        }
        return re;
    }

    /**
     * 获取国家名值对映射
     * 
     * <pre>
     * {"CN":"中国"}
     * </pre>
     * 
     * @param list
     *            国家对象列表
     * @param lang
     *            语言
     * @return 一个对家名值对映射
     */
    private static NutMap getCountryNameMap(List<LbsCountry> list, String lang) {
        NutMap re = new NutMap();
        for (LbsCountry lc : list) {
            String key = lc.getKey();
            Object val = lc.getName(lang, true);
            re.put(key, val);
        }
        return re;
    }

    /**
     * 获取国家列表，形式类似:
     * 
     * <pre>
     * [{key:"CN", name:"中国"}...]
     * </pre>
     * 
     * @param list
     *            国家对象列表
     * @param lang
     *            语言
     * @return 一个国家列表
     */
    private static List<NutMap> getCountries(List<LbsCountry> list, String lang) {
        List<NutMap> list2 = new ArrayList<>(list.size());
        for (LbsCountry lc : list) {
            list2.add(lc.toMap(lang));
        }
        return list2;
    }

}
