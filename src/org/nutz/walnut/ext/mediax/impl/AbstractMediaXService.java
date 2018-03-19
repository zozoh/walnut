package org.nutz.walnut.ext.mediax.impl;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.born.Borning;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.resource.Scans;
import org.nutz.walnut.ext.mediax.MediaXAPI;
import org.nutz.walnut.ext.mediax.MediaXService;
import org.nutz.walnut.ext.mediax.apis.AbstractMediaXAPI;
import org.nutz.walnut.ext.mediax.bean.MxAccount;

/**
 * 作为所有 MeidaXService 的父类。主要提供一些便捷的 MediaXAPI 的创建帮助函数等
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractMediaXService implements MediaXService {

    private static final Log log = Logs.get();

    private Map<String, Borning<MediaXAPI>> map;

    /**
     * 在构造函数
     */
    @SuppressWarnings("unchecked")
    public AbstractMediaXService() {
        // 扫描本包下的所有类
        List<Class<?>> list = Scans.me().scanPackage(AbstractMediaXAPI.class);
        for (Class<?> klass : list) {
            // 跳过抽象类
            int mod = klass.getModifiers();
            if (Modifier.isAbstract(mod))
                continue;

            // 跳过非公共的类
            if (!Modifier.isPublic(klass.getModifiers()))
                continue;

            // 跳过内部类
            if (klass.getName().contains("$"))
                continue;

            // 如果是 HopeHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(MediaXAPI.class)) {
                // 获取 Key: 格式为 CHINAZMEIDAXAPI
                String nm = klass.getSimpleName().toUpperCase();

                // 不符合规范
                if (!nm.endsWith("MEIDAXAPI")) {
                    // throw Er.create("e.cmd." + myName + ".wrongHdlName",
                    // klass.getName());
                    if (log.isWarnEnabled())
                        log.warnf("MediaXAPI class <%s> invalid name foramt!!!",
                                  klass.getSimpleName());
                    continue;
                }
                // 来吧
                String key = nm.substring(0, nm.length() - "MEIDAXAPI".length());
                Class<MediaXAPI> k2 = (Class<MediaXAPI>) klass;
                Borning<MediaXAPI> borning = Mirror.me(k2).getBorningByArgTypes(MxAccount.class);
                map.put(key, borning);
            }
        }
    }

    /**
     * 给子类用的一个便捷的创建账号的帮助函数
     * 
     * @param account
     *            账号
     * @return 对应媒体 API 实例
     */
    protected MediaXAPI _create(MxAccount account) {
        if (null == account)
            return null;
        // 得到创建器
        Borning<MediaXAPI> borning = map.get(account.mtype.toString());
        if (null == borning) {
            throw Lang.makeThrow("Unsupport MxAType '%s' : %s",
                                 account.mtype.toString(),
                                 Json.toJson(account, JsonFormat.forLook()));
        }
        // 来，创建吧
        return borning.born(account);
    }

}
