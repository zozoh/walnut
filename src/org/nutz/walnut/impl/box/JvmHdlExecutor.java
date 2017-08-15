package org.nutz.walnut.impl.box;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

public abstract class JvmHdlExecutor extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 准备上下文
        JvmHdlContext hc = new JvmHdlContext();
        hc.ioc = this.ioc;
        hc.sys = sys;

        // 初始化上下文（子类可以重载）
        hc.args = args;
        this._find_hdl_name(sys, hc);

        // 首先看第一个参数是不是能找到一个控制器
        hc.hdl = hdls.get(hc.hdlName);

        // 最后统一解析参数
        this._parse_params(sys, hc);

        // 如果找不到
        if (null == hc.hdl) {
            throw Er.create("e.cmd." + myName + ".unknown.hdl", hc.hdlName);
        }

        // 调用之前
        _before_invoke(sys, hc);

        // 调用执行器
        hc.hdl.invoke(sys, hc);

        // 最后执行
        _before_quit(sys, hc);
    }

    protected void _before_invoke(WnSystem sys, JvmHdlContext hc) {}

    protected void _before_quit(WnSystem sys, JvmHdlContext hc) {}

    protected void _find_hdl_name(WnSystem sys, JvmHdlContext hc) {
        if (hc.args.length == 0)
            return;
        // 默认第一个参数为处理器名称
        hc.hdlName = hc.args[0];

        // 后面的参数作为处理器参数
        hc.args = Arrays.copyOfRange(hc.args, 1, hc.args.length);
    }

    protected void _find_hdl_name_with_conf(WnSystem sys,
                                            JvmHdlContext hc,
                                            String dirName,
                                            String confName) {
        // 如果第一个参数就是处理器，那么，HOME 则自动寻找
        if (hc.args.length < 1) {
            throw Er.create("e.cmd." + dirName + ".lackArgs", hc.args);
        }

        int pos;

        // 第一个参数就是 hdl，那么就不设置 home，可能有的命令不需要 Home
        if (null != this.getHdl(hc.args[0])) {
            hc.hdlName = hc.args[0];
            hc.oRefer = null;
            pos = 1;
        }
        // 第一个参数必须为微信公众号目录名
        else {
            if (hc.args.length < 2) {
                throw Er.create("e.cmd.ftp.lackArgs", hc.args);
            }
            // 得到公众号
            String pnb = hc.args[0];

            // 表示当前目录为主目录
            if (".".equals(pnb)) {
                hc.oRefer = sys.getCurrentObj();
            }
            // 获取主目录
            else {
                String aph = Wn.normalizeFullPath("~/." + dirName + "/" + hc.args[0], sys);
                hc.oRefer = sys.io.check(null, aph);
            }

            if (hc.oRefer != null && hc.oRefer.isLink()) {
                String aph = Wn.normalizeFullPath("~/." + dirName + "/" + hc.oRefer.link(), sys);
                hc.oRefer = sys.io.check(null, aph);
            }

            // 获得处理器名称
            hc.hdlName = hc.args[1];

            // 处理参数的位置
            pos = 2;
        }

        // 解析参数
        hc.args = Arrays.copyOfRange(hc.args, pos, hc.args.length);

        // 得到配置文件
        if (null != hc.oRefer) {
            WnObj oConf = sys.io.check(hc.oRefer, confName);
            hc.setv(confName + "_obj", oConf);
        }
    }

    protected void _parse_params(WnSystem sys, JvmHdlContext hc) {
        hc.parseParams(hc.args);
        hc.jfmt = Cmds.gen_json_format(hc.params);
    }

    protected String myName;

    private Map<String, JvmHdl> hdls;

    protected JvmHdl getHdl(String hdlName) {
        return hdls.get(hdlName);
    }

    public JvmHdlExecutor() {
        // 分析自身的类名
        this.myName = this.getClass().getSimpleName().toLowerCase();
        if (myName.startsWith("cmd_"))
            myName = myName.substring(4);

        // 开始扫描
        hdls = new HashMap<String, JvmHdl>();
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
            if (mi.isOf(JvmHdl.class)) {
                // 获取 Key
                String nm = klass.getSimpleName().toLowerCase();
                if (!nm.startsWith(myName + "_")) {
                    // throw Er.create("e.cmd." + myName + ".wrongHdlName",
                    // klass.getName());
                    continue;
                }
                String key = nm.substring(myName.length() + 1).toLowerCase();
                JvmHdl hdl = (JvmHdl) mi.born();
                hdls.put(key, hdl);
            }
        }
    }

}
