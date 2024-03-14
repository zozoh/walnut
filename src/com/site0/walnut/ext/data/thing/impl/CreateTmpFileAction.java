package com.site0.walnut.ext.data.thing.impl;

import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.data.thing.ThingAction;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wtime;

public class CreateTmpFileAction extends ThingAction<WnObj> {

    public String fileName;

    public String duration;

    @Override
    public WnObj invoke() {
        // 默认放一天
        long duInMs = Wtime.millisecond(Strings.sBlank(duration, "1d"));

        // 创建临时文件
        String fnm = Strings.sBlank(fileName, "tmp_${id}");
        WnObj oTmpd = Things.dirTsTmpFile(io, oTs);
        WnObj oTmp = io.createIfNoExists(oTmpd, fnm, WnRace.FILE);

        // 设置过期时间
        if (duInMs > 0) {
            oTmp.expireTime(Wn.now() + duInMs);
            io.set(oTmp, "^expi$");
        }

        // 返回吧
        return oTmp;
    }

}
