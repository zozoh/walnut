package com.site0.walnut;

import java.io.File;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.util.Wlog;

/**
 * 记录一下版本号
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public final class WnVersion {

    // 编译时，会自动读取 pom.xml 来获取版本了
    private static String _version = "0.0";
    private static String _alias = "DEV";

    private static final Log log = Wlog.getMAIN();

    static {
        File f = Files.findFile("version.json");
        if (null == f) {
            String aph = "/opt/wn4j/core/conf/version.json";
            log.infof("Fail to findVersion from classPath, try '%s'", aph);
            f = new File(aph);
        }
        if (null != f && f.exists()) {
            log.infof("Try loadVersion from file: '%s'", f);
            NutMap vinfo = Json.fromJsonFile(NutMap.class, f);
            _version = vinfo.getString("version", _version);
            _alias = vinfo.getString("alias", _alias);
        } else {
            log.warnf("Version File Not Exists: %s", f);
        }
    }

    /**
     * @return 版本号
     */
    public static String get() {
        return _version;
    }

    /**
     * @return 版本代号
     */
    public static String alias() {
        return _alias;
    }

    /**
     * @return 一个完整的全描述的版本信息
     */
    public static String getName() {
        return String.format("Walnut%s(%s)", get(), alias());
    }

    // 禁止实例化
    private WnVersion() {}
}
