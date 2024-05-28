package com.site0.walnut.impl.box;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.resource.Scans;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public abstract class JvmFilterExecutor<C extends JvmFilterContext, T extends JvmFilter<C>>
        extends JvmExecutor {

    protected Class<T> filterType;

    protected Class<C> contextType;

    protected String myName;

    protected Map<String, T> hdls;

    public JvmFilterExecutor(Class<C> contextType, Class<T> filterType) {
        this.contextType = contextType;
        this.filterType = filterType;
        this.init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        // 分析自身的类名
        this.myName = this.getClass().getSimpleName().toLowerCase();
        if (myName.startsWith("cmd_"))
            myName = myName.substring(4);

        // 开始扫描
        hdls = new HashMap<>();

        // 扫描本包下的所有类
        List<Class<?>> list = Scans.me()
                                   .scanPackage(this.getClass().getPackage().getName() + ".hdl");
        for (Class<?> klass : list) {
            // 跳过抽象类
            int mod = klass.getModifiers();
            if (Modifier.isAbstract(mod))
                continue;

            // 跳过非公共的类
            if (!Modifier.isPublic(mod))
                continue;

            // 跳过内部类
            if (klass.getName().contains("$"))
                continue;

            // 如果是 Hdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(filterType)) {
                // 获取 Key
                String nm = klass.getSimpleName().toLowerCase();
                if (!nm.startsWith(myName + "_")) {
                    // throw Er.create("e.cmd." + myName + ".wrongHdlName",
                    // klass.getName());
                    continue;
                }
                String key = nm.substring(myName.length() + 1).toLowerCase();
                T hdl = (T) mi.born();
                hdls.put(key, hdl);
            }
        }
    }

    @Override
    public String[] prepareArgs(String[] args) {
        return args;
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        List<String> vals = new ArrayList<>(args.length);

        // 成对的调用序列
        List<T> hdlFilters = new ArrayList<>(args.length);
        List<ZParams> hdlParams = new ArrayList<>(args.length);

        // 记录最后一个
        T lastHdl = null;
        List<String> lastArgs = new ArrayList<>(args.length);

        // 开始循环
        for (String arg : args) {
            // 发现 hdl
            if (arg.startsWith("@")) {
                T hdl = this.hdls.get(arg.substring(1));
                if (null == hdl) {
                    String cmdName = this.getClass().getSimpleName();
                    throw Err.create("e.cmd." + cmdName + ".invalid_hdl", arg);
                }
                // 存入最后一个
                if (null != lastHdl) {
                    String[] h_args = lastArgs.toArray(new String[lastArgs.size()]);
                    ZParams params = lastHdl.parseParams(h_args);
                    // 参数和执行器成对加入
                    hdlFilters.add(lastHdl);
                    hdlParams.add(params);
                }
                // 开启新的
                lastHdl = hdl;
                lastArgs.clear();

                continue;
            }
            // 拆包字符串
            if (Ws.isQuoteBy(arg, '"', '"') || Ws.isQuoteBy(arg, '\'', '\'')) {
                arg = arg.substring(1, arg.length() - 1);
            }
            // 存入 hdl 参数
            if (null != lastHdl) {
                lastArgs.add(arg);
            }
            // 存入全局
            else {
                vals.add(arg);
            }
        }

        // 存入最后一个
        if (null != lastHdl) {
            String[] h_args = lastArgs.toArray(new String[lastArgs.size()]);
            ZParams params = lastHdl.parseParams(h_args);
            // 参数和执行器成对加入
            hdlFilters.add(lastHdl);
            hdlParams.add(params);
        }

        // 准备上下文
        C fc = this.newContext();
        fc.sys = sys;
        fc.params = parseParams(vals.toArray(new String[vals.size()]));
        fc.jfmt = Cmds.gen_json_format(fc.params);

        // 准备输入对象
        prepare(sys, fc);

        // 依次调用处理器
        try {
            _exec_filters(sys, hdlFilters, hdlParams, fc);
        }
        finally {
            this.onFinished(sys, fc);
        }

        // 处理输出
        output(sys, fc);
    }

    protected void _exec_filters(WnSystem sys, List<T> hdlFilters, List<ZParams> hdlParams, C fc) {
        int n = hdlFilters.size();
        for (int i = 0; i < n; i++) {
            T hdl = hdlFilters.get(i);
            ZParams params = hdlParams.get(i);
            hdl.process(sys, fc, params);
            if (fc.isBreakExec()) {
                break;
            }
        }
    }

    protected void onFinished(WnSystem sys, C context) {}

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    abstract protected C newContext();

    abstract protected void prepare(WnSystem sys, C fc);

    abstract protected void output(WnSystem sys, C fc);

}
