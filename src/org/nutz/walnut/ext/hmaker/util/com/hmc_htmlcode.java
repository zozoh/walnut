package org.nutz.walnut.ext.hmaker.util.com;

import org.jsoup.nodes.Element;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;

public class hmc_htmlcode extends AbstractSimpleCom {

    @Override
    protected String getArenaClassName() {
        return "hmc-htmlcode";
    }

    @Override
    protected boolean doArena(HmPageTranslating ing, Element eleArena) {
        eleArena.html(ing.propCom.getString("code"));
        return true;
    }

    @Override
    public boolean isDynamic(Element eleCom) {
        return false;
    }
}
