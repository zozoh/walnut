package org.nutz.walnut.ext.data.thing.options;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.WnOutputable;

public class ThCreateOptions {

    public static ThCreateOptions create(String uniqueKey,
                                         NutMap fixedMeta,
                                         WnExecutable executor,
                                         String cmdTmpl) {
        ThCreateOptions opt = new ThCreateOptions();
        opt.uniqueKey = uniqueKey;
        opt.fixedMeta = fixedMeta;
        opt.executor = executor;
        opt.cmdTmpl = cmdTmpl;
        return opt;
    }

    public static ThCreateOptions create(String uniqueKey,
                                         NutMap fixedMeta,
                                         WnOutputable out,
                                         String process,
                                         WnExecutable executor,
                                         String cmdTmpl) {
        ThCreateOptions opt = new ThCreateOptions();
        opt.uniqueKey = uniqueKey;
        opt.fixedMeta = fixedMeta;
        opt.out = out;
        opt.process = process;
        opt.executor = executor;
        opt.cmdTmpl = cmdTmpl;
        return opt;
    }

    public String uniqueKey;
    public NutMap fixedMeta;
    public WnOutputable out;
    public String process;
    public WnExecutable executor;
    public String cmdTmpl;
    public boolean withoutHook;
}
