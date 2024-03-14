package com.site0.walnut.ext.data.fake;

import org.nutz.json.Json;
import com.site0.walnut.ext.data.fake.out.FakeSysOutput;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;

public class cmd_fake extends JvmFilterExecutor<FakeContext, FakeFilter> {

    public cmd_fake() {
        super(FakeContext.class, FakeFilter.class);
    }

    @Override
    protected FakeContext newContext() {
        return new FakeContext();
    }

    @Override
    protected void prepare(WnSystem sys, FakeContext fc) {
        fc.number = fc.params.val_int(0, 1);
        fc.output = new FakeSysOutput(sys);
    }

    @Override
    protected void output(WnSystem sys, FakeContext fc) {
        // 逐个输出
        for (int i = 0; i < fc.number; i++) {
            Object v = fc.faker.next();
            fc.output.write(v);
        }

        // 最后输入出 Beans 的 JSON
        if (null != fc.beans) {
            String json = Json.toJson(fc.beans, fc.jfmt);
            sys.out.println(json);
        }
    }

}
