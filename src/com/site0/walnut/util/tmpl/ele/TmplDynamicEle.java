package com.site0.walnut.util.tmpl.ele;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;

public abstract class TmplDynamicEle implements TmplEle {

    private String content;

    private String _type;

    protected String key;

    // 标识键是否包括 '[' ']' 或者 '.' 等可以被 Mapl 解析的取值路径
    private boolean _is_key_as_path;

    private String _org_fmt;

    private String _dft_val;

    private String _dft_key;

    protected String fmt;

    protected TmplDynamicEle() {}

    protected TmplDynamicEle(String type, String key, String fmt, String dft_str) {
        this._type = type;
        this.key = key;
        this._is_key_as_path = key.indexOf('.') > 0
                               || (key.indexOf('[') > 0 && key.indexOf(']') > 0);
        this._org_fmt = fmt;

        // 默认值取 key @xxx
        if (!Strings.isBlank(dft_str) && (dft_str.startsWith("@") || dft_str.startsWith("="))) {
            this._dft_key = dft_str.substring(1);
        }
        // 默认值是静态的
        else {
            this._dft_val = dft_str;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("${").append(key);
        if (null != _type) {
            sb.append('<').append(_type);
            if (null != _org_fmt) {
                sb.append(':').append(_org_fmt);
            }
            sb.append('>');
        }
        // 默认键
        if (null != _dft_key) {
            sb.append('?').append('@').append(_dft_val);
        }
        // 默认值
        else if (null != _dft_val) {
            sb.append('?').append(_dft_val);
        }
        return sb.append('}').toString();
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        // 看看有没有值
        Object val = __get_val(rc.context, key);

        // 试图用默认键取值
        if (null == val) {
            // 默认键
            if (null != _dft_key) {
                val = __get_val(rc.context, _dft_key);
            }
            // 默认值
            else if (null != _dft_val) {
                val = _dft_val;
            }
        }

        // 转换成字符串
        String str = _val(val);

        // 如果木值
        if (null == str) {
            if (rc.showKey) {
                rc.out.append("${").append(key).append('}');
            }
        }
        // 否则填充
        else {
            rc.out.append(str);
        }
    }

    private Object __get_val(NutBean context, String k) {
        // 得到值
        Object val = context.getOr(k);

        // 如果没值，看看是否需要用 mapl 搞一下
        if (null == val && _is_key_as_path) {
            val = Mapl.cell(context, k);
        }
        return val;
    }

    protected abstract String _val(Object val);

    public String getKey() {
        return key;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    public TmplDynamicEle setContent(String content) {
        this.content = content;
        return this;
    }

}
