package org.nutz.walnut.ext.data.fake.hdl;

import java.util.ArrayList;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.fake.FakeContext;
import org.nutz.walnut.ext.data.fake.FakeFilter;
import org.nutz.walnut.ext.data.fake.impl.WnBeanFaker;
import org.nutz.walnut.ext.data.fake.out.FakeListOutput;
import org.nutz.walnut.ext.data.fake.out.FakeObjOutput;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

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
        NutMap map = Lang.map(json);
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
