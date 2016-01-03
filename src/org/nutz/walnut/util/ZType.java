package org.nutz.walnut.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 检查文件类型的辅助类
 * 
 * @author pw
 *
 */
public class ZType {

    private static Log log = Logs.get();

    // category -> [tp, tp, tp]
    public static Map<String, Set<String>> cate2tplist = new HashMap<String, Set<String>>();
    // tp -> [cate, cate, cate]
    public static Map<String, Set<String>> tp2catelist = new HashMap<String, Set<String>>();

    public static void loadCategory(PropertiesProxy cgpp) {
        synchronized (cate2tplist) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            for (String cg : cgpp.getKeys()) {
                sb.append("    -").append(cg).append(":\n     ");
                Set<String> tplist = cate2tplist.get(cg);
                if (tplist == null) {
                    tplist = new HashSet<String>();
                    cate2tplist.put(cg, tplist);
                }
                sb.append(Strings.join2(",", cgpp.getList(cg).toArray())).append("\n");
                for (String tp : cgpp.getList(cg)) {
                    tplist.add(tp);
                    // catelist
                    Set<String> catelist = tp2catelist.get(tp);
                    if (catelist == null) {
                        catelist = new HashSet<String>();
                        tp2catelist.put(tp, catelist);
                    }
                    if (!catelist.contains(cg)) {
                        catelist.add(cg);
                    }
                }
            }
            log.debugf("Load Categorys: %s", sb.substring(0, sb.length() - 1));
        }
    }

    public static Set<String> getCategory(String tp) {
        return tp2catelist.get(tp);
    }

    public static Set<String> getType(String cate) {
        return cate2tplist.get(cate);
    }

    // --------------------------------- 默认的几种类型

    private static boolean _isXXX(String cate, String tp) {
        return getType(cate).contains(tp.toLowerCase());
    }

    public static boolean isImage(String tp) {
        return _isXXX("image", tp);
    }

    public static boolean isVideo(String tp) {
        return _isXXX("video", tp);
    }

    public static boolean isText(String tp) {
        return _isXXX("text", tp);
    }

    public static boolean isSouceCode(String tp) {
        return _isXXX("sourceCode", tp);
    }

    public static boolean isExcel(String tp) {
        return _isXXX("excel", tp);
    }

}
