package com.site0.walnut.ext.data.titanium.hdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.data.titanium.WnI18nService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;

@JvmHdlParamArgs(value = "cqn", regex = "^(json)$")
public class ti_i18n implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析参数
        String lang = hc.params.getString("lang", "zh-cn");
        String load = hc.params.getString("load", "/rs/ti/i18n/");

        // 准备输入
        String input = Cmds.getParamOrPipe(sys, hc.params, 0);

        if (Ws.isBlank(input)) {
            return;
        }

        // 分析参数
        boolean dftAsJson = false;
        Object inputObj;
        if (Ws.isQuoteBy(input, '{', '}') || Ws.isQuoteBy(input, '[', ']')) {
            inputObj = Json.fromJson(input);
            dftAsJson = true;
        } else {
            inputObj = input;
        }

        // 准备接口
        WnI18nService i18ns = createI18nService(sys, lang, load);

        // 循环处理输入
        String output;
        Object re = asAny(i18ns, lang, inputObj);
        boolean asJson = hc.params.is(dftAsJson, "json");
        if (asJson) {
            JsonFormat jfmt = Cmds.gen_json_format(hc.params);
            output = Json.toJson(re, jfmt);
        }
        // 直接文本输出
        else {
            output = Castors.me().castToString(re);
        }

        sys.out.println(output);

    }

    public static WnI18nService createI18nService(WnSystem sys, String lang, String load) {
        WnI18nService i18ns = new WnI18nService(sys);

        // 加载目录
        String[] loadPaths = Ws.splitIgnoreBlank(load, "[;:]");
        for (String loadPath : loadPaths) {
            i18ns.load(loadPath, lang);
        }
        return i18ns;
    }

    @SuppressWarnings("unchecked")
    private Object asAny(WnI18nService i18ns, String lang, Object input) {
        if (null == input) {
            return input;
        }
        if (input instanceof Map<?, ?>) {
            return asMap(i18ns, lang, (Map<String, Object>) input);
        }
        if (input instanceof Collection<?>) {
            return asList(i18ns, lang, (Collection<Object>) input);
        }
        if (input.getClass().isArray()) {
            return asArray(i18ns, lang, (Object[]) input);
        }
        return asString(i18ns, lang, input.toString());
    }

    private NutMap asMap(WnI18nService i18ns, String lang, Map<String, Object> input) {
        NutMap re = new NutMap();
        for (Map.Entry<String, Object> en : input.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            Object v2 = asAny(i18ns, lang, val);
            re.put(key, v2);
        }
        return re;
    }

    private Object[] asArray(WnI18nService i18ns, String lang, Object[] input) {
        Object[] re = new Object[input.length];
        for (int i = 0; i < input.length; i++) {
            Object val = input[i];
            Object v2 = asAny(i18ns, lang, val);
            re[i] = v2;
        }
        return re;

    }

    private List<Object> asList(WnI18nService i18ns, String lang, Collection<Object> input) {
        List<Object> re = new ArrayList<>(input.size());
        for (Object val : input) {
            Object v2 = asAny(i18ns, lang, val);
            re.add(v2);
        }
        return re;

    }

    private String asString(WnI18nService i18ns, String lang, String input) {
        return i18ns.getText(lang, input);
    }

}
