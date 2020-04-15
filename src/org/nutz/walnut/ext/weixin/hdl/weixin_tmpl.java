package org.nutz.walnut.ext.weixin.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.weixin.bean.WxTemplateData;
import org.nutz.weixin.spi.WxResp;

@JvmHdlParamArgs("cqn")
public class weixin_tmpl implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 获得 API 接口
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);

        // 获取 ..
        if (hc.params.has("get")) {
            String getMode = hc.params.get("get");
            // # 获取行业信息
            // demo@~$ weixin xxx tmpl -get industry
            if ("industry".equals(getMode)) {
                WxResp re = wxApi.get_industry();
                sys.out.println(Json.toJson(re, hc.jfmt));
            }
            // # 列出所有模板
            // demo@~$ weixin xxx tmpl -get
            else {
                WxResp re = wxApi.get_all_private_template();
                // 打印模板详情
                if (re.containsKey("template_list")) {
                    sys.out.println(Json.toJson(re.get("template_list"), hc.jfmt));
                }
                // 打印空数组
                else {
                    sys.err.println(Json.toJson(re, hc.jfmt));
                }
            }
        }
        // # 添加一个模板得到模板的 ID
        // demo@~$ weixin xxx tmpl -add TM00015
        // Doclyl5uP7Aciu-qZ7mJNPtWkbkYnWBWVja26EGbNyk
        else if (hc.params.has("add")) {
            String template_id_short = hc.params.get("add");
            WxResp re = wxApi.template_api_add_template(template_id_short);
            sys.out.println(Json.toJson(re, hc.jfmt));
        }
        // # 删除一个模板
        // demo@~$ weixin xxx tmpl -del
        // iPk5sOIt5X_flOVKn5GrTFpncEYTojx6ddbt8WYoV5s
        else if (hc.params.has("del")) {
            String template_id = hc.params.get("del");
            WxResp re = wxApi.template_api_del_template(template_id);
            sys.out.println(Json.toJson(re, hc.jfmt));
        }
        // # 设置行业
        // demo@~$ weixin xxx tmpl -industry 2,5
        else if (hc.params.has("industry")) {
            String[] ss = Strings.splitIgnoreBlank(hc.params.get("industry"));
            if (ss.length != 2) {
                throw Er.create("e.cmd.weixin.tmpl.set_industry.invalid", hc.get("industry"));
            }
            WxResp re = wxApi.template_api_set_industry(ss[0], ss[1]);
            sys.out.println(Json.toJson(re, hc.jfmt));
        }
        //
        // # 发送模板消息
        // demo@~$ weixin xxx tmpl -send "{..}"
        // -url http:xxx
        // -tid iPk5sOIt5X_flOVKn5GrTFpncEYTojx6ddbt8WYoV5s
        // -to OPENID
        else if (hc.params.has("send")) {
            // 读取发送内容
            String json = Cmds.checkParamOrPipe(sys, hc.params, "send", true);

            // 解析 ...
            NutMap map = Lang.map(json);
            Map<String, WxTemplateData> data = new HashMap<String, WxTemplateData>();

            // 格式化 map 的内容
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();

                // 忽略空值
                if (null == val)
                    continue;

                // 转换 ..
                data.put(key, new WxTemplateData(val));
            }

            // 检查其他必要函数
            String url = hc.params.get("url");
            String tid = hc.params.check("tid");
            String to = hc.params.check("to");

            // // 支持本地别名
            // if (tid.startsWith("nm:")) {
            // String tmplName = tid.substring(3);
            // WnObj t = sys.io.check(wxApi.getHomeObj(), "tmpl/"+tmplName);
            // tid = t.getString("weixin_tid");
            // }

            // 调用接口
            WxResp re = wxApi.template_send(to, tid, url, data);
            if (!"true".equals(hc.params.get("quite")))
                sys.out.println(Json.toJson(re, hc.jfmt));
        }

    }

}
