package org.nutz.walnut.ext.lbs.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.lbs.bean.LbsChina;
import org.nutz.walnut.ext.lbs.bean.LbsChinaAddr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.ajax.Ajax;

@JvmHdlParamArgs(value = "cqn", regex = "^(list|ajax|json)$")
public class lbs_cn implements JvmHdl {

    private static final LbsChina CHINA = LbsChina.getInstance();

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        Object re;

        // 得到输出
        if (hc.params.is("list")) {
            // 查询某个地址下面的子地址
            if (hc.params.vals.length > 0) {
                String code = _tidy_code(hc.params.val(0));
                re = CHINA.getAddressList(code);
            }
            // 那就是获取顶级地址咯
            else {
                re = CHINA.getAddressList(null);
            }
        }
        // 不是列表模式，那么看看是否是读取一个地址
        else if (hc.params.vals.length == 1) {
            String code = _tidy_code(hc.params.val(0));
            re = CHINA.getAddress(code);
        }
        // 读取多个地址?
        else if (hc.params.vals.length > 1) {
            List<LbsChinaAddr> list = new ArrayList<>(hc.params.vals.length);
            for (String val : hc.params.vals) {
                String code = this._tidy_code(val);
                LbsChinaAddr lca = CHINA.getAddress(code);
                if (null != lca)
                    list.add(lca);
            }
            re = list;
        }
        // 默认就获取顶级列表吧
        else {
            re = CHINA.getAddressList(null);
        }

        // 准备输出
        if (hc.params.is("ajax")) {
            re = Ajax.ok().setData(re);
            sys.out.println(Json.toJson(re, hc.jfmt));
        }
        // 输出 JSON
        else if (hc.params.is("json")) {
            sys.out.println(Json.toJson(re, hc.jfmt));
        }
        // 纯粹输出内容
        else {
            Lang.each(re, new Each<LbsChinaAddr>() {
                public void invoke(int index, LbsChinaAddr lca, int length) {
                    sys.out.printlnf("%d. %s", index + 1, lca);
                }
            });
        }

    }

    private String _tidy_code(String code) {
        // 整理为12位地址编码
        if (code.length() > 6) {
            return Strings.alignLeft(code, 12, '0');
        }
        // 不到 6 位的整理为 6 位
        return Strings.alignLeft(code, 6, '0');
    }

}