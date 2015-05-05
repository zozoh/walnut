package org.nutz.walnut.impl.io;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.walnut.api.io.WnObj;

public class WnObjMetaInputStream extends StringInputStream {

    public WnObjMetaInputStream(WnObj o) {
        this(o, JsonFormat.forLook().setQuoteName(true));
    }

    public WnObjMetaInputStream(WnObj o, JsonFormat fmt) {
        super(Json.toJson(o, fmt));
    }

}
