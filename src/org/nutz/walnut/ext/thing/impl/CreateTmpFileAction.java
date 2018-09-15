package org.nutz.walnut.ext.thing.impl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public class CreateTmpFileAction extends ThingAction<WnObj> {

    public String fileName;

    public String duration;

    @Override
    public WnObj invoke() {
        // 默认放一天
        long duInMs = Wn.msValueOf(Strings.sBlank(duration, "1d"));

        // 创建临时文件
        String fnm = Strings.sBlank(fileName, "tmp_${id}");
        WnObj oTmpd = Things.dirTsTmpFile(io, oTs);
        WnObj oTmp = io.createIfNoExists(oTmpd, fnm, WnRace.FILE);

        // 设置过期时间
        if (duInMs > 0) {
            oTmp.expireTime(System.currentTimeMillis() + duInMs);
            io.set(oTmp, "^expi$");
        }

        // 返回吧
        return oTmp;
    }

}
