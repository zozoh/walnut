package com.site0.walnut.util.tmpl.ele;

import com.site0.walnut.util.str.WnStrCaseConvertor;
import com.site0.walnut.util.str.WnStrCases;

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
