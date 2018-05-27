package org.nutz.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Maths;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class cmd_cp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "pvr");
        if (params.vals.length != 2) {
            throw Err.create("e.cmds.cp.not_enugh_args");
        }
        String ph_src = Wn.normalizeFullPath(params.vals[0], sys);
        String ph_dst = Wn.normalizeFullPath(params.vals[1], sys);
        ph_dst = Disks.getCanonicalPath(ph_dst);

        // 得到源
        List<WnObj> oSrcList = Cmds.evalCandidateObjsNoEmpty(sys, Lang.array(ph_src), 0);

        // 准备 copy 模式
        int mode = 0;
        if (params.is("r"))
            mode |= Wn.Io.RECUR;
        if (params.is("p"))
            mode |= Wn.Io.PROP;
        if (params.is("v"))
            mode |= Wn.Io.VERBOSE;

        // 执行 copy
        for (WnObj oSrc : oSrcList)
            _do_copy(sys, mode, oSrc, ph_dst);
    }

    /**
     * @param sys
     *            系统上下文
     * @param mode
     *            copy 位串： RECUR 或者 CREATE_PARENTS
     * @param oSrc
     *            源对象
     * @param ph_dst
     *            目标路径
     * 
     * @see org.nutz.walnut.util.Wn.Io#RECUR
     * @see org.nutz.walnut.util.Wn.Io#PROP
     */
    protected static void _do_copy(WnSystem sys, int mode, WnObj oSrc, String ph_dst) {
        // 得到配置信息
        boolean isR = Maths.isMask(mode, Wn.Io.RECUR);

        // 检查源，如果是目录，则必须标识 -r
        if (oSrc.isDIR() && !isR) {
            throw Err.create("e.io.copy.src_is_dir", oSrc);
        }

        // 输出相对路径的基础路径，即当前路径
        String ph_base = sys.getCurrentObj().getRegularPath();

        // 得到目标
        WnObj oDst = Wn.getObj(sys, ph_dst);

        // 目标不存在:
        // 应该先检查一下其父是否存在，如果不存在看看是不是 -p
        // 总之要创建一个目标出来
        if (null == oDst) {
            // 必须存在父
            if (!isR) {
                WnObj oP = Wn.checkObj(sys, Files.getParent(ph_dst));
                oDst = sys.io.createIfNoExists(oP, Files.getName(ph_dst), oSrc.race());
            }
            // 否则自由创建
            else {
                oDst = sys.io.createIfNoExists(null, ph_dst, oSrc.race());
            }
            // 执行 Copy 就好了
            __recur_copy_obj(sys, mode, ph_base, oSrc, oDst);
        }
        // 否则，不能是自己 copy 给自己就好
        else {
            // 自己 copy 自己，不能够啊
            if (oDst.isSameId(oSrc)) {
                throw Er.create("e.io.copy.self", oSrc);
            }
            // 目标是一个文件夹
            if (oDst.isDIR()) {
                // 在里面创建与源同名的目标
                WnObj oDst2 = sys.io.createIfNoExists(oDst, oSrc.name(), oSrc.race());
                // 执行 Copy
                __recur_copy_obj(sys, mode, ph_base, oSrc, oDst2);
            }
            // 目标是一个文件
            else if (oDst.isFILE()) {
                // 源必须是一个文件
                if (!oSrc.isFILE()) {
                    throw Er.create("e.io.copy.file2dir", oSrc.path() + " ->> " + oDst.path());
                }
                // 执行 Copy
                __recur_copy_obj(sys, mode, ph_base, oSrc, oDst);
            }
            // 靠！什么鬼！
            else {
                throw Lang.impossible();
            }
        }
    }

    private static void __recur_copy_obj(WnSystem sys,
                                         int mode,
                                         String ph_base,
                                         WnObj oSrc,
                                         WnObj oDst) {
        // 得到配置信息
        boolean isP = Maths.isMask(mode, Wn.Io.PROP);
        boolean isV = Maths.isMask(mode, Wn.Io.VERBOSE);

        // 如果是文件夹
        if (oSrc.isDIR() && oDst.isDIR()) {
            Wn.Io.eachChildren(sys.io, oSrc, new Each<WnObj>() {
                public void invoke(int index, WnObj o, int length) {
                    WnObj oTa = sys.io.createIfNoExists(oDst, o.name(), o.race());
                    __recur_copy_obj(sys, mode, ph_base, o, oTa);
                }
            });
        }
        // 如果是文件
        else if (oSrc.isFILE() && oDst.isFILE()) {
            // 输出日志
            if (isV) {
                String rph = Disks.getRelativePath(ph_base, oSrc.path());
                sys.out.println(rph);
            }

            // 内容 copy
            Wn.Io.copyFile(sys.io, oSrc, oDst);

            // 元数据 copy
            if (isP) {
                NutBean meta = oSrc.pickBy("!^(id|pid|race|ph|nm|d[0-9]|ct|lm|data|sha1|len|thumb|videoc_dir)$");
                // meta.put("mode", oSrc.mode());
                // meta.put("group", oSrc.group());
                // meta.put("tp", oSrc.type());
                // meta.put("mime", oSrc.mime());
                sys.io.appendMeta(oDst, meta);
            }
        }
        // 靠，不可能
        else {
            throw Lang.impossible();
        }

    }

}
