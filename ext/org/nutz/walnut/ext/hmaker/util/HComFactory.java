package org.nutz.walnut.ext.hmaker.util;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;

public class HComFactory {

    private Map<String, HComHandler> comHdls;

    public HComFactory() {
        // 开始扫描
        comHdls = new HashMap<String, HComHandler>();
        // 扫描本包下的所有类
        List<Class<?>> list = Scans.me().scanPackage(this.getClass());
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

            // 得到控件类型
            HComType comType = klass.getAnnotation(HComType.class);
            if (null == comType)
                continue;

            // 如果是 HopeHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(HComHandler.class)) {
                HComHandler hdl = (HComHandler) mi.born();
                comHdls.put(comType.value(), hdl);
            }
        }
    }

    public HComHandler check(String type) {
        HComHandler cmdHdl = comHdls.get(type);
        if (null == cmdHdl)
            throw Er.create("e.hmaker.unknown.comType", type);
        return cmdHdl;
    }

}
