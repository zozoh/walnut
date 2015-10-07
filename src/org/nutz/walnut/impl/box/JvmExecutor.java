package org.nutz.walnut.impl.box;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public abstract class JvmExecutor {

    protected Ioc ioc;

    public abstract void exec(WnSystem sys, String[] args) throws Exception;

    public String getManual() {
        Class<?> klass = this.getClass();
        String ph = klass.getPackage().getName().replace('.', '/');
        ph += "/" + klass.getSimpleName() + ".man";

        File f = Files.findFile(ph);
        if (null == f) {
            return klass.getSimpleName() + " ??? no manual";
        }

        return Files.read(f);
    }

    public String getMyName() {
        String nm = this.getClass().getSimpleName();
        return nm.substring("cmd_".length());
    }

    protected WnObj getCurrentObj(WnSystem sys) {
        String pwd = sys.se.envs().getString("PWD");
        String path = Wn.normalizePath(pwd, sys);
        return sys.io.check(null, path);
    }

    protected WnObj getHome(WnSystem sys) {
        String pwd = sys.se.envs().getString("HOME");
        String path = Wn.normalizePath(pwd, sys);
        return sys.io.check(null, path);
    }

    protected void joinObjByPath(WnSystem sys, final List<WnObj> list, WnObj p, String str) {
        // 分析路径
        str = Wn.normalizePath(str, sys);
        
        // 看看是否需要回到根
        if(str.startsWith("/") || null==p){
            p = sys.io.getRoot();
        }
        
        // 将路径拆分成数组
        String[] ss = Strings.splitIgnoreBlank(str, "/");

        // 根节点
        if (ss.length == 0) {
            list.add(p);
            return;
        }
        // 试图按路径查找
        // 如果路径中有 id:xxx 那么就应该从这个位置开始
        // 尝试从后查找，如果有 id:xxx 那么就截断，因为前面的就木有意义了
        int off = 0;
        for (int i = ss.length - 1; i >= 0; i--) {
            String nm = ss[i];
            if (nm.startsWith("id:")) {
                p = sys.io.checkById(nm.substring(3));
                off = i + 1;
                break;
            }
        }

        // 直接干到结尾了
        if (off >= ss.length){
            list.add(p);
            return;
        }

        // 递归查找
        __find_last_level_objs(sys.io, p, ss, off, list);

    }

    private void __find_last_level_objs(final WnIo io,
                                        WnObj p,
                                        final String[] ss,
                                        final int off,
                                        final List<WnObj> list) {
        String nm = ss.length > off ? ss[off] : null;

        // 当前目录
        if (".".equals(nm)) {
            __find_last_level_objs_handle(io, ss, off + 1, list, p);
        }
        // 回退一级
        else if ("..".equals(nm)) {
            WnObj o = p.parent();
            __find_last_level_objs_handle(io, ss, off + 1, list, o);
        }
        // 根据 ID
        else if (nm.startsWith("id:")) {
            WnObj o = io.checkById(nm.substring("id:".length()));
            __find_last_level_objs(io, o, ss, off + 1, list);
        }
        // 继续查找
        else {
            WnQuery q = Wn.Q.pid(p.id()).setv("nm", nm);
            io.each(q, new Each<WnObj>() {
                public void invoke(int index, WnObj o, int length) {
                    __find_last_level_objs_handle(io, ss, off + 1, list, o);
                }
            });
        }
    }

    private void __find_last_level_objs_handle(final WnIo io,
                                               final String[] ss,
                                               final int off,
                                               final List<WnObj> list,
                                               WnObj o) {
        // 如果到了最后一层，才加入 list
        if (off >= ss.length) {
            if (null == o)
                list.add(io.getRoot());
            else
                list.add(o);
        }
        // 否则继续递归
        else if (!o.isFILE()) {
            __find_last_level_objs(io, o, ss, off, list);
        }
    }

    protected List<WnObj> evalCandidateObjsNoEmpty(WnSystem sys,
                                                   String[] paths,
                                                   boolean joinCurrent) {
        LinkedList<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, paths, list, joinCurrent);
        checkCandidateObjsNoEmpty(paths, list);
        return list;
    }

    protected List<WnObj> evalCandidateObjs(WnSystem sys, String[] paths, boolean joinCurrent) {
        LinkedList<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, paths, list, joinCurrent);
        return list;
    }

    protected WnObj evalCandidateObjsNoEmpty(WnSystem sys,
                                             String[] paths,
                                             final List<WnObj> list,
                                             boolean joinCurrent) {
        WnObj re = evalCandidateObjs(sys, paths, list, joinCurrent);
        checkCandidateObjsNoEmpty(paths, list);
        return re;
    }

    protected WnObj evalCandidateObjs(WnSystem sys,
                                      String[] paths,
                                      final List<WnObj> list,
                                      boolean joinCurrent) {
        // 得到当前目录
        WnObj p = getCurrentObj(sys);

        // 计算要列出的目录
        // 没参数认为是当前目录
        if (paths.length == 0) {
            if (joinCurrent) {
                list.add(p);
            }
        }
        // 否则根据路径归纳需要列的目录
        else {
            for (String val : paths) {
                joinObjByPath(sys, list, p, val);
            }
        }

        return p;
    }

    protected void checkCandidateObjsNoEmpty(String[] args, List<WnObj> list) {
        if (list.isEmpty()) {
            throw Er.create("e.io.obj.noexists", Lang.concat(", ", args));
        }
    }

    protected WnObj getObj(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, null);
        List<WnObj> list = new LinkedList<WnObj>();
        evalCandidateObjs(sys, params.vals, list, false);
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
