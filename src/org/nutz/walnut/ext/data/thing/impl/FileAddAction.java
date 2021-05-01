package org.nutz.walnut.ext.data.thing.impl;

import java.io.InputStream;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.thing.ThingDataAction;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.util.Wn;

public class FileAddAction extends ThingDataAction<WnObj> {

    public String fnm;

    public Object src;

    public String dupp;

    public boolean overwrite;

    @Override
    public WnObj invoke() {
        WnObj oDir = this.myDir();
        WnObj oSrc = null;
        if (null != src && src instanceof WnObj) {
            oSrc = (WnObj) src;
        }

        // ..............................................
        // 首先判断文件是否存在
        WnObj oM = io.fetch(oDir, fnm);

        // ..............................................
        // 不存在，创建一个
        if (null == oM) {
            oM = io.create(oDir, fnm, WnRace.FILE);
        }
        // 如果存在 ...
        else {
            // 如果存在，并且还要 -read 自己，那么就直接过
            if (null != oSrc && oSrc.isSameId(oM)) {
                return oM;
            }

            // 是否生成一个新的
            if (!Strings.isBlank(dupp)) {
                oM = Things.createFileNoDup(io, oDir, fnm, dupp);
            }
            // 不能生成一个新的，并且还不能覆盖就抛错
            else if (!this.overwrite) {
                throw Er.create("e.thing.file.exists", this.myFilePath(fnm));
            }
        }
        // ..............................................
        // 嗯得到一个空文件了，那么看看怎么写入内容呢？
        if (null != src) {
            // 从输出流中读取
            if (src instanceof InputStream) {
                io.writeAndClose(oM, (InputStream) src);
            }
            // 否则试图从指定的文件里读取
            else if (null != oSrc) {
                io.copyData(oSrc, oM);
                // 因为 copyData 是快速命令，所以要重新执行一下钩子
                oM = Wn.WC().doHook("write", oM);
            }
        }

        // 指定了 dir 就更新计数
        if (this.hasDirName(fnm)) {
            String dnm = this.myFileDirName(fnm);
            Things.update_file_count(io, oT, dnm, _Q());
        }

        // 返回
        return oM;
    }

}
