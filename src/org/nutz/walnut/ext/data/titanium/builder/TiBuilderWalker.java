package org.nutz.walnut.ext.data.titanium.builder;

import org.nutz.walnut.api.io.WnObj;

public interface TiBuilderWalker {

    void run(int index, WnObj f, String rph, String[] lines) throws Exception;

}
