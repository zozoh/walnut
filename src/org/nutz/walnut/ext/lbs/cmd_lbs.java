package org.nutz.walnut.ext.lbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.walnut.ext.lbs.bean.LbsCountry;
import org.nutz.walnut.impl.box.JvmHdlExecutor;

public class cmd_lbs extends JvmHdlExecutor {

    private static List<LbsCountry> countriesList;

    private static Map<String, LbsCountry> countriesMap;

    static {
        String str = Files.read("org/nutz/walnut/ext/lbs/data/countries.json");
        countriesList = Json.fromJsonAsList(LbsCountry.class, str);

        countriesMap = new HashMap<>();
        for (LbsCountry lc : countriesList) {
            countriesMap.put(lc.getKey(), lc);
        }
    }

    /**
     * 根据国家代码，获取有限的几个国家对象组成的列表
     * 
     * @param keys
     *            国家代码列表，譬如 <code>CN</code>
     * 
     * @return 国家对象列表
     */
    public static List<LbsCountry> getCountryObjListBy(String... keys) {
        List<LbsCountry> list = new ArrayList<>(keys.length);
        for (String key : keys) {
            LbsCountry lc = countriesMap.get(key);
            if (null != lc) {
                list.add(lc);
            }
        }
        return list;
    }

    /**
     * 根据国家代码，获取某个国家对象
     * 
     * @param key
     *            国家代码，譬如 <code>CN</code>
     * @return 国家对象
     */
    public static LbsCountry getCountryObj(String key) {
        return countriesMap.get(key);
    }

    /**
     * 获取国家对象对列表
     * 
     * <pre>
     * [{
     *   key:"CN", 
     *   name: {
     *       zh_cn: "中国", 
     *       en_us: "China"
     *   }
     * }]
     * </pre>
     * 
     * @return 获取国家对象对列表
     */
    public static List<LbsCountry> getCountryObjList() {
        return countriesList;
    }

    /**
     * 获取国家名值对映射表
     * 
     * <pre>
     * {
     *   "CN": {
     *      key:"CN", 
     *      name: {
     *          zh_cn: "中国", 
     *          en_us: "China"
     *      }
     *   }
     * }
     * </pre>
     * 
     * @return 一个对家名值对映射
     */
    public static Map<String, LbsCountry> getCountryObjMap() {
        return countriesMap;
    }

}
