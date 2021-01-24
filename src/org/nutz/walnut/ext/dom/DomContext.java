package org.nutz.walnut.ext.dom;

import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class DomContext extends JvmFilterContext {

    public CheapDocument doc;

    public CheapElement current;

}
