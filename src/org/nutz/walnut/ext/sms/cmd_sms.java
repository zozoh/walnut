package org.nutz.walnut.ext.sms;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class cmd_sms extends JvmHdlExecutor {

    public static final String KEY_PROVIDER = "provider";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_VARS = "vars";

    private Map<String, SmsProvider> providers;

    public cmd_sms() {
        super();
        this.__reload_providers();
    }

    @Override
    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        // 默认采用 send
        if (hc.args.length == 0 || !hc.args[0].matches("^(send|query)$")) {
            hc.hdlName = "send";
        }
        // 第一个参数为处理器名称
        else {
            hc.hdlName = hc.args[0];
            // 后面的参数作为处理器参数
            hc.args = Arrays.copyOfRange(hc.args, 1, hc.args.length);
        }
    }

    @Override
    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) {
        // 这里，根据参数，加载 provider 的配置信息，以及对应的 provider
        String provNm = hc.params.get(KEY_PROVIDER, "Yunpian");
        SmsProvider provider = providers.get(provNm.toLowerCase());
        if (null == provider) {
            throw Er.create("e.cmd.sms.provider.unsupport", provNm);
        }
        // ............................................
        // 得到配置主目录
        hc.oRefer = Wn.checkObj(sys, "~/.sms");
        // 默认配置文件
        String confNm = hc.params.get(KEY_CONFIG);
        WnObj oConf = sys.io.check(hc.oRefer, Strings.sBlank(confNm, "config_" + provNm));
        NutMap config = sys.io.readJson(oConf, NutMap.class);
        // ............................................
        NutMap vars;
        if (hc.params.has("vars")) {
            vars = hc.params.getMap("vars");
        } else {
            vars = new NutMap();
        }
        // ............................................
        // 记录 provider 到属性里
        hc.attrs().put(KEY_PROVIDER, provider);
        hc.attrs().put(KEY_CONFIG, config);
        hc.attrs().put(KEY_VARS, vars);
    }

    private void __reload_providers() {
        // 开始扫描
        providers = new HashMap<String, SmsProvider>();
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

            // 如果是 HopeHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(SmsProvider.class)) {
                // 获取 Key
                String nm = klass.getSimpleName().toLowerCase();
                if (nm.endsWith("smsprovider")) {
                    String key = nm.substring(0, nm.length() - "smsprovider".length());
                    SmsProvider provider = (SmsProvider) mi.born();
                    providers.put(key, provider);
                }
            }
        }
    }
}
