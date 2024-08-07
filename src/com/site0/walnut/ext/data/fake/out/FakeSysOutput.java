package com.site0.walnut.ext.data.fake.out;

import com.site0.walnut.ext.data.fake.FakeOutput;
import com.site0.walnut.impl.box.WnSystem;

public class FakeSysOutput implements FakeOutput {

    private WnSystem sys;

    public FakeSysOutput(WnSystem sys) {
        this.sys = sys;
    }

    @Override
    public void write(Object obj) {
        if (null == obj) {
            sys.out.println();
        } else {
            sys.out.println(obj);
        }
    }

}
