package org.nutz.walnut.util.tmpl.ele;

import org.nutz.walnut.util.str.WnStrCaseConvertor;
import org.nutz.walnut.util.str.WnStrCases;

public class StrCaseCovertor implements StrEleConvertor {

    private WnStrCaseConvertor cc;

    public StrCaseCovertor(String mode) {
        cc = WnStrCases.check(mode);
    }

    @Override
    public String process(String str) {
        return cc.covert(str);
    }

}
