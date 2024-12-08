package com.site0.walnut.ext.media.edi.loader;

import java.util.HashMap;

import com.site0.walnut.ext.media.edi.newloader.CLNTDUPLoader;
import com.site0.walnut.ext.media.edi.newloader.CLREGRLoader;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UNH;

/**
 * 封装报文解析的高级类的工厂方法
 *
 * @author zozoh(zozohtnt @ gmail.com)
 */
public abstract class EdiMsgs {

    private static HashMap<String, EdiMsgLoader<?>> loaders = new HashMap<>();

    static {
        loaders.put("CONTRL", new CONTRLLoader());
        loaders.put("CLREG", new CLREGLoader());
        loaders.put("CLREGR", new CLREGRLoader());
        loaders.put("CLNTDUP", new CLNTDUPLoader());
        // AirCargoReport Response
        loaders.put("AIRCRR", new AIRCRRLoader());
        loaders.put("SEACRR", new SEACRRLoader());
        loaders.put("CARST", new CARSTLoader());
    }

    public static String getLoaderType(EdiMessage msg) {
        ICS_UNH unh = msg.getHeader();
        if (unh.isType("CONTRL")) {
            return "CONTRL";
        }
        // 根据BGM 判断
        if (unh.isType("^(CUSRES|CUSCAR)$")) {
            EdiSegment seg = msg.findSegment("BGM");
            NutBean bean = new NutMap();
            seg.fillBean(bean, null, "code,,,type");
            // 可能为 CLREGR 或者 CLNTDUP 等
            if (bean.has("type")) {
                return bean.getString("type");
            }
        }
        throw Er.create("e.edi.failLoaderType", msg.toString());
    }

    public static EdiMsgLoader<?> getLoader(String key) {
        return loaders.get(key);
    }

    public static EdiMsgLoader<?> checkLoader(String key) {
        EdiMsgLoader<?> loader = getLoader(key);
        if (null == loader) {
            throw Er.create("e.cmd.edi.loader.NoDefined", key);
        }
        return loader;
    }

    public static CONTRLLoader getInterchangeLoader() {
        return (CONTRLLoader) loaders.get("CONTRL");
    }

    public static CLREGRLoader getCLREGRLoader() {
        return (CLREGRLoader) loaders.get("CLREGR");
    }

    public static CLNTDUPLoader getCLNTDUPLoader() {
        return (CLNTDUPLoader) loaders.get("CLNTDUP");
    }

}
