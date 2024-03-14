package com.site0.walnut.ext.sys.root;

import java.io.File;
import java.io.InputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;

public class cmd_lccp extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "fv");

        sys.nosecurity(new Atom() {
            public void run() {
                __do_it(sys, params);
            }
        });
    }

    private void __do_it(WnSystem sys, ZParams params) {
        WnAccount me = sys.getMe();

        // 确保当前账号是 root 组成员
        if (!sys.auth.isMemberOfGroup(me, "root")) {
            throw Er.create("e.cmd.lccp.nopvg");
        }

        Log log = null;
        if (params.is("v"))
            log = sys.getLog(params);

        // 得到源文件
        String phSrc = params.val_check(0);
        File src = Files.findFile(phSrc);
        if (null == src) {
            throw Er.create("e.cmd.lccp.src.noexists", phSrc);
        }

        // 得到目标文件
        WnObj p = sys.getCurrentObj();
        String phTa = params.val(1);
        String aphTa = null;
        WnObj oTa;
        // 默认采用当前目录，与本地同名
        if (null == phTa) {
            oTa = sys.io.fetch(p, src.getName());
        }
        // 否则试图获取一下
        else {
            aphTa = Wn.normalizeFullPath(phTa, sys);
            oTa = sys.io.fetch(p, aphTa);
        }

        // 不存在的话，就创建
        if (null == oTa) {
            // 给定了路径
            if (null != phTa) {
                // 且路径是一个目录 ...
                if (phTa.endsWith("/")) {
                    WnObj oDir = sys.io.create(p, aphTa, WnRace.DIR);
                    oTa = sys.io.create(oDir, src.getName(), WnRace.FILE);
                }
                // 那么路径就是最终的文件名
                else {
                    oTa = sys.io.create(null, aphTa, WnRace.FILE);
                }
            }
            // 没给路径，就在当前目录下创建
            else {
                oTa = sys.io.create(p, src.getName(), WnRace.FILE);
            }
        }
        // 存在的话
        else {
            // 目录，就在之内创建
            if (oTa.isDIR()) {
                p = oTa;
                oTa = sys.io.fetch(p, src.getName());
            }
            // 文件的话，看看要不要覆盖
            if (oTa.isFILE()) {
                if (!params.is("f"))
                    throw Er.create("e.cmd.lccp.exists", oTa);
            }
            // 还是目录，靠，不能写入
            else {
                throw Er.create("e.cmd.lccp.writeDir", oTa);
            }
        }
        // 执行写入
        if (null != log)
            log.info(oTa.path());

        InputStream ins = Streams.fileIn(src);
        sys.io.writeAndClose(oTa, ins);

        if (null != log)
            log.infof(" - writed %d bytes", oTa.len());
    }

}
