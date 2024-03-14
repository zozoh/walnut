package com.site0.walnut.ext.net.sms.hdl;

import java.io.IOException;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Times;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import com.site0.walnut.ext.net.sms.SmsProvider;
import com.site0.walnut.ext.net.sms.SmsQuery;
import com.site0.walnut.ext.net.sms.cmd_sms;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wtime;
import org.nutz.web.WebException;

@JvmHdlParamArgs("cqnl")
public class sms_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws IOException {
        // 准备查询条件
        SmsQuery q = new SmsQuery();
        NutMap conf = hc.attrs().getAs(cmd_sms.KEY_CONFIG, NutMap.class);
        // ............................................
        // 首先得到时间区间
        String str = hc.params.val(0, "10m");

        // 试图当做时间范围
        try {
            long ms = Wtime.millisecond(str);
            q.to = Times.now();
            q.from = Times.D(q.to.getTime() - ms);
        }
        // 那么就是时间区间咯
        catch (WebException e) {
            DateRegion dr = Region.Date(str);
            q.from = dr.left();
            q.to = dr.right();
        }
        // ............................................
        // 得到分页信息
        q.pageSize = hc.params.getInt("pgsz", -1);
        q.pageNumber = hc.params.getInt("pn", -1);
        q.vars = hc.attrs().getAs(cmd_sms.KEY_VARS, NutMap.class);
        // ............................................
        // 执行查询
        SmsProvider provider = hc.attrs().getAs(cmd_sms.KEY_PROVIDER, SmsProvider.class);
        String json = provider.query(conf, q);

        // 处理查询结果
        List<Object> list = Json.fromJsonAsList(Object.class, json);

        // 仅输出一个
        if (!hc.params.is("l") && list.size() == 1) {
            sys.out.println(Json.toJson(list.get(0), hc.jfmt));
        }
        // 输出列表
        else {
            sys.out.println(Json.toJson(list, hc.jfmt));
        }

    }

}
