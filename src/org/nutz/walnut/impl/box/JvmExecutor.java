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
        return this.getManual(null, "zh_cn");
    }

    public String getManual(String lang) {
        return this.getManual(null, lang);
    }

    public String getManual(String hdlName, String lang) {
        Class<?> klass = this.getClass();
        String ph = klass.getPackage().getName().replace('.', '/');

        // 帮助文件 majorName
        String majorName = klass.getSimpleName();
        if (!Strings.isBlank(hdlName)) {
            majorName += "_" + hdlName;
        }
        // 新版本: 寻找 man/zh_ch/${xxx}.md
        String aph = Wn.appendPath(ph, "man", lang, majorName + ".md");
        File f = Files.findFile(aph);

        // 老版本：优先找 .md
        if (null == f) {
            aph = Wn.appendPath(ph, majorName + ".md");
            f = Files.findFile(aph);
        }

        // 最初版本：没有的话，找 .man
        if (null == f) {
            aph = Wn.appendPath(ph, majorName + ".man");
            f = Files.findFile(aph);
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
