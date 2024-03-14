package com.site0.walnut.ext.data.fake.out;

import java.util.Date;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.ext.data.fake.FakeOutput;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wtime;

public class FakeObjOutput implements FakeOutput {

    private WnSystem sys;

    private WnObj oDir;

    private boolean verbose;

    private int index;

    public FakeObjOutput(WnSystem sys, WnObj oDir, boolean verbose) {
        this.sys = sys;
        this.oDir = oDir;
        this.verbose = verbose;
    }

    @Override
    public void write(Object o) {
        if (null != o && o instanceof NutBean) {
            NutBean meta = (NutBean) o;
            WnIoObj obj = new WnIoObj();
            obj.update(meta);

            WnObj o2 = sys.io.create(oDir, obj);
            if (this.verbose) {
                Date d = new Date(o2.createTime());
                String s = Wtime.format(d, "yyyy-MM-dd HH:mm:ss");
                sys.out.printlnf("%d) %s: %s", this.index++, o2.name(), s);
            }
        }
    }

}
