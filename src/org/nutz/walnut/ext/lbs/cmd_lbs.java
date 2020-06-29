package org.nutz.walnut.ext.lbs;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.lbs.bean.LbsCountry;
import org.nutz.walnut.impl.box.JvmHdlExecutor;

public class cmd_lbs extends JvmHdlExecutor {

    private static List<LbsCountry> countries;

    static {
        String str = Files.read("org/nutz/walnut/ext/lbs/data/countries.json");
        countries = Json.fromJsonAsList(LbsCountry.class, str);
    }

    public static List<LbsCountry> getCountries() {
        return countries;
    }

    public static List<NutMap> getCountries(String lang) {
        List<NutMap> list = new ArrayList<>(countries.size());
        for (LbsCountry lc : countries) {
            list.add(lc.toMap(lang));
        }
        return list;
    }

}
