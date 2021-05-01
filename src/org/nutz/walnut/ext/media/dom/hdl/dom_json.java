package org.nutz.walnut.ext.media.dom.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.flt.CheapAttrNameFilter;
import org.nutz.walnut.cheap.dom.flt.CheapRegexAttrNameFilter;
import org.nutz.walnut.ext.media.dom.DomContext;
import org.nutz.walnut.ext.media.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class dom_json extends DomFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        fc.quiet = true;
        // 防守
        if (!fc.hasSelected()) {
            return;
        }

        // 分析参数
        JsonFormat jfmt = Cmds.gen_json_format(params);
        WnMatch[] mTags = new WnMatch[params.vals.length];
        String[] elAttr = new String[params.vals.length];

        // 分析属性名称转换
        String name = params.getString("name");
        CheapAttrNameFilter attrNf = null;
        if (!Ws.isBlank(name)) {
            attrNf = new CheapRegexAttrNameFilter(name);
        }

        // 分析子节点属性映射
        for (int i = 0; i < params.vals.length; i++) {
            String val = params.val(i);
            int pos = val.indexOf('=');
            if (pos <= 0) {
                continue;
            }
            String elTag = val.substring(0, pos).trim();
            String elVal = val.substring(pos + 1).trim();
            mTags[i] = new AutoMatch(elTag.toUpperCase());
            elAttr[i] = elVal;
        }

        // 准备循环处理元素
        List<NutBean> beans = new LinkedList<>();
        for (CheapElement el : fc.selected) {
            // 准备对应对象
            NutMap bean = new NutMap();
            bean.putAll(el.getAttrObj(attrNf));
            beans.add(bean);

            // 处理子节点
            List<CheapElement> children = el.getChildElements();
            for (CheapElement child : children) {
                for (int x = 0; x < mTags.length; x++) {
                    WnMatch mTag = mTags[x];
                    String stdName = child.getStdTagName();
                    if (mTag.match(stdName)) {
                        String key = child.getTagName();
                        String k2 = key;
                        if (null != attrNf) {
                            k2 = attrNf.getName(key);
                        }
                        String atName = elAttr[x];
                        // 是否为自动 Java 值呢
                        boolean autoJavaValue = false;
                        if (atName.startsWith("=")) {
                            autoJavaValue = true;
                            atName = atName.substring(1);
                        }
                        // 得到值
                        String val;
                        if ("!TEXT".equalsIgnoreCase(atName)) {
                            val = child.getText();
                        } else {
                            val = child.attr(atName);
                        }
                        val = Ws.trim(val);
                        // 转换
                        Object v2 = val;
                        if (autoJavaValue) {
                            v2 = Ws.toJavaValue(val);
                        }
                        // 记入
                        bean.put(k2, v2);
                    }
                }
            }
        }

        // 需要变成 Map
        String mapKey = params.getString("map");
        if (!Ws.isBlank(mapKey)) {
            // 防守
            if (beans.isEmpty()) {
                sys.out.println("{}");
                return;
            }
            NutMap re = new NutMap();
            for (NutBean bean : beans) {
                String key = bean.getString(mapKey);
                if (null != key) {
                    re.put(key, bean);
                }
            }
            sys.out.println(Json.toJson(re, jfmt));
        }
        // 否则作为列表输出
        else {
            sys.out.println(Json.toJson(beans, jfmt));
        }

    }

}
