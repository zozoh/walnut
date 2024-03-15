package com.site0.walnut.ext.data.fake.hdl;

import java.util.ArrayList;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.fake.FakeContext;
import com.site0.walnut.ext.data.fake.FakeFilter;
import com.site0.walnut.ext.data.fake.impl.WnBeanFaker;
import com.site0.walnut.ext.data.fake.out.FakeListOutput;
import com.site0.walnut.ext.data.fake.out.FakeObjOutput;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class fake_bean extends FakeFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "v");
    }

    @Override
    protected void process(WnSystem sys, FakeContext fc, ZParams params) {
        // 获得语言
        String lang = sys.getLang();
        lang = params.get("lang", lang);
        lang = Ws.snakeCase(lang);

        // 获得模拟对象生成配置
        String json = Cmds.getParamOrPipe(sys, params, 0);
        NutMap map = Wlang.map(json);
        fc.faker = new WnBeanFaker(lang, map);

        // 输出到目录里
        String ph = params.getString("to");
        if (!Ws.isBlank(ph)) {
            boolean verbose = params.is("v");
            WnObj o = Wn.checkObj(sys, ph);
            fc.output = new FakeObjOutput(sys, o, verbose);
        }
        // 输出
        else {
            fc.beans = new ArrayList<>(fc.number);
            fc.output = new FakeListOutput(fc.beans);
        }
    }

}
