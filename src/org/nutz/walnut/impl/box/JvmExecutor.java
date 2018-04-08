package org.nutz.walnut.impl.box;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public abstract class JvmExecutor {

    protected Ioc ioc;

    public abstract void exec(WnSystem sys, String[] args) throws Exception;

    public String getManual() {
        return this.getManual(null);
    }

    public String getManual(String hdlName) {
        Class<?> klass = this.getClass();
        String ph = klass.getPackage().getName().replace('.', '/');
        ph += "/" + klass.getSimpleName();
        if (!Strings.isBlank(hdlName)) {
            ph += "_" + hdlName;
        }
        // 优先找 .md
        File f = Files.findFile(ph + ".md");

        // 没有的话，找 .man
        if (null == f) {
            f = Files.findFile(ph + ".man");
        }

        // 没 manual，抛错
        if (null == f) {
            return klass.getSimpleName() + " ??? no manual";
        }

        return Files.read(f);
    }

    public String getMyName() {
        String nm = this.getClass().getSimpleName();
        if (nm.startsWith("cmd_"))
            nm = nm.substring("cmd_".length());
        return Strings.lowerWord(nm, '-');
    }

    /**
     * 这个在未来的某个版本，将会被删除，<code>WnSystem</code> 提供了代替方法
     * 
     * @see org.nutz.walnut.impl.box.WnSystem#getCurrentObj()
     */
    @Deprecated
    protected WnObj getCurrentObj(WnSystem sys) {
        String pwd = sys.se.vars().getString("PWD");
        String path = Wn.normalizePath(pwd, sys);
        WnObj re = sys.io.check(null, path);
        return Wn.WC().whenEnter(re, false);
    }

    /**
     * 这个在未来的某个版本，将会被删除，<code>WnSystem</code> 提供了代替方法
     * 
     * @see org.nutz.walnut.impl.box.WnSystem#getHome()
     */
    @Deprecated
    protected WnObj getHome(WnSystem sys) {
        String pwd = sys.se.vars().getString("HOME");
        String path = Wn.normalizePath(pwd, sys);
        return sys.io.check(null, path);
    }

    protected WnObj getObj(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, null);
        List<WnObj> list = new LinkedList<WnObj>();
        Cmds.evalCandidateObjs(sys, params.vals, list, 0);
        if (list.size() <= 0) {
            sys.err.print("need a obj");
            return null;
        }
        if (list.size() > 1) {
            sys.err.print("too many objs, only handler one obj at once");
            return null;
        }
        // 默认只处理第一个
        return list.get(0);
    }

}
