package com.site0.walnut.util.tmpl.ele;

public class StrFormatConvertor implements StrEleConvertor {

    private String fmt;

    StrFormatConvertor(String fmt) {
        this.fmt = fmt;
    }

    @Override
    public String process(String str) {
        if (null == fmt)
            return str;
        return String.format(fmt, str);
    }

}
