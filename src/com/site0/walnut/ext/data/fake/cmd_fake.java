package com.site0.walnut.ext.data.fake;

import org.nutz.json.Json;
import com.site0.walnut.ext.data.fake.out.FakeSysOutput;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class cmd_fake extends JvmFilterExecutor<FakeContext, FakeFilter> {

    public cmd_fake() {
        super(FakeContext.class, FakeFilter.class);
    }

    @Override
    protected FakeContext newContext() {
        return new FakeContext();
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
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
            Object obj = fc.beans;
            if (!fc.params.is("l") && 1 == fc.beans.size()) {
                obj = fc.beans.get(0);
            }
            String json = Json.toJson(obj, fc.jfmt);
            sys.out.println(json);
        }
    }

}
