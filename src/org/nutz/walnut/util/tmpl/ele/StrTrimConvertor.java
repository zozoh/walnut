package org.nutz.walnut.util.tmpl.ele;

class StrTrimConvertor implements StrEleConvertor {

    @Override
    public String process(String str) {
        return str.trim();
    }

}
