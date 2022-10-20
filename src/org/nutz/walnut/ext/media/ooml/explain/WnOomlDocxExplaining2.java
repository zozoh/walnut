package org.nutz.walnut.ext.media.ooml.explain;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.ext.media.ooml.api.OomlExplaining;
import org.nutz.walnut.ooml.OomlPackage;

public class WnOomlDocxExplaining2 implements OomlExplaining {

    private OomlPackage ooml;

    private CheapResourceLoader loader;

    public WnOomlDocxExplaining2(OomlPackage ooml, CheapResourceLoader loader) {
        this.ooml = ooml;
        this.loader = loader;
    }

    @Override
    public void explain(NutBean vars) {}

}
