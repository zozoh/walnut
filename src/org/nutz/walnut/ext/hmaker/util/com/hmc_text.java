package org.nutz.walnut.ext.hmaker.util.com;

import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_text extends AbstractComHanlder {

    @Override
    protected void _exec(HmPageTranslating ing) {

        ing.addMyRule(null, ing.cssBlock);
        if (ing.propCom.size() > 0)
            ing.addMyRule(".hmc-text", ing.propCom);

    }

}
