package org.nutz.walnut.ext.hmaker.util.com;

import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_htmlcode extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {
        ing.eleCom.empty().html(ing.propCom.getString("code"));
    }

}
