package com.site0.walnut.ext.data.domain.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.domain.DomainContext;
import com.site0.walnut.ext.data.domain.DomainFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class domain_list extends DomainFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnbish", "^(json|pager)$");
    }

    @Override
    protected void process(WnSystem sys, DomainContext fc, ZParams params) {
        // 获取静默模式和JSON输出标志
        fc.quiet = true;
        boolean isJSON = params.is("json");
        JsonFormat jfmt = Cmds.gen_json_format(params);

        // 获取域名存放主目录
        WnObj oDmnHome = sys.io.check(null, "/domain");
        String flts = params.val(0);

        // 准备翻页信息
        WnPager wp = new WnPager(params);

        // 准备查询条件
        NutMap flt = new NutMap();
        WnQuery q = Wn.Q.pid(oDmnHome);
        if (!Ws.isBlank(flts)) {
            flt = Wlang.map(flts);
            q.setAll(flt);
        }
        flt.put("race", "FILE");

        // 设置排序
        NutMap sort = params.getMap("sort");
        if (null != sort && !sort.isEmpty()) {
            q.sort(sort);
        } else {
            q.sort(Wlang.map("nm", 1));
        }

        // 获取域目录下的所有对象
        wp.setupQuery(sys, q);
        List<WnObj> objs = sys.io.query(q);

        // 根据参数决定输出格式
        if (isJSON) {
            String json = Json.toJson(objs, jfmt);
            sys.out.println(json);
            return;
        }

        // 准备输出字段
        String[] cols = Wlang.array("nm",
                                    "tp",
                                    "domain",
                                    "site",
                                    "expi:time",
                                    "ct:time",
                                    "lm:time");
        // 准备参数
        boolean showBorder = params.is("b");
        boolean showHeader = params.is("h");
        boolean showSummary = params.is("s");
        boolean showIndex = params.is("i");
        int indexBase = params.getInt("ibase", 0);

        // 输出
        Cmds.output_objs_as_table(sys,
                                  wp,
                                  objs,
                                  cols,
                                  showBorder,
                                  showHeader,
                                  showSummary,
                                  showIndex,
                                  indexBase);
    }

}