package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsOrderByElement extends SqlVarsElement {

    public VarsAsOrderByElement(String content) {
        super(content);
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        // 获取对应的排序字段
        NutBean bean = this.getBean(rc.context);

        // 排序无需参数
        List<String> sorts = new ArrayList<>(bean.size());

        // 计入整体动态前缀
        if (!Ws.isBlank(this.prefix) && !sorts.isEmpty()) {
            rc.out.append(' ').append(this.prefix).append(' ');
        }

        // 增加排序字段
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            String key = en.getKey();
            // 忽略上下文里的隐藏字段
            if (key.startsWith("__") || key.startsWith("$")) {
                continue;
            }
            // 增加字段前缀
            if (null != this.fieldPrefix) {
                key = this.fieldPrefix + key;
            }
            Object val = en.getValue();
            String srt = "ASC";
            if (null != val) {
                if ("desc".equalsIgnoreCase(val.toString())) {
                    srt = "DESC";
                } else if (val instanceof Number) {
                    if (((Number) val).intValue() < 0) {
                        srt = "DESC";
                    }
                }
            }
            sorts.add(String.format("%s %s", key, srt));
        }

        // 记入输出
        if (!sorts.isEmpty()) {
            rc.out.append(Ws.join(sorts, ", "));
        }
        // 默认排序
        else if (this.hasDefaultValue()) {
            rc.out.append(this.getDefaultValue());
        }
    }

}
