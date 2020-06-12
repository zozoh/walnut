package org.nutz.walnut.ext.jsonx;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.resource.Scans;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class cmd_jsonx extends JvmExecutor {

    private String myName;

    protected Map<String, JsonXFilter> hdls;

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        List<String> vals = new ArrayList<>(args.length);

        // 成对的调用序列
        List<JsonXFilter> hdlFilters = new ArrayList<>(args.length);
        List<ZParams> hdlParams = new ArrayList<>(args.length);

        // 记录最后一个
        JsonXFilter lastHdl = null;
        List<String> lastArgs = new ArrayList<>(args.length);

        // 开始循环
        for (String arg : args) {
            JsonXFilter hdl = this.hdls.get(arg);

            // 发现 hdl
            if (null != hdl) {
                // 存入最后一个
                if (null != lastHdl) {
                    String[] h_args = lastArgs.toArray(new String[lastArgs.size()]);
                    ZParams params = ZParams.parse(h_args, null);
                    // 参数和执行器成对加入
                    hdlFilters.add(lastHdl);
                    hdlParams.add(params);
                }
                // 开启新的
                lastHdl = hdl;
                lastArgs.clear();
            }
            // 存入 hdl 参数
            else if (null != lastHdl) {
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
            ZParams params = ZParams.parse(h_args, null);
            // 参数和执行器成对加入
            hdlFilters.add(lastHdl);
            hdlParams.add(params);
        }

        // 准备上下文
        JsonXContext ctx = new JsonXContext();
        ctx.sys = sys;
        ctx.params = ZParams.parse(vals.toArray(new String[vals.size()]), "cqn");
        ctx.jfmt = Cmds.gen_json_format(ctx.params);

        // 准备输入对象
        String json = sys.in.readAll();
        if (!Strings.isBlank(json)) {
            ctx.obj = Json.fromJson(json);
        }

        // 依次调用处理器
        int n = hdlFilters.size();

        for (int i = 0; i < n; i++) {
            JsonXFilter hdl = hdlFilters.get(i);
            ZParams params = hdlParams.get(i);
            hdl.process(sys, ctx, params);
        }

        // 处理输出
        String output = Json.toJson(ctx.obj, ctx.jfmt);
        sys.out.println(output);
    }

    public cmd_jsonx() {
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
            if (mi.isOf(JsonXFilter.class)) {
                // 获取 Key
                String nm = klass.getSimpleName().toLowerCase();
                if (!nm.startsWith(myName + "_")) {
                    // throw Er.create("e.cmd." + myName + ".wrongHdlName",
                    // klass.getName());
                    continue;
                }
                String key = nm.substring(myName.length() + 1).toLowerCase();
                JsonXFilter hdl = (JsonXFilter) mi.born();
                hdls.put(key, hdl);
            }
        }
    }

}
