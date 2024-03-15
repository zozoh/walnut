package org.nutz.json.handler;

import java.io.IOException;

import org.nutz.json.JsonFormat;
import org.nutz.json.JsonRender;
import org.nutz.json.JsonTypeHandler;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;

/**
 * 
 * @author wendal
 *
 */
public class JsonClassHandler extends JsonTypeHandler {

    public boolean supportFromJson(Mirror<?> mirror, Object obj) {
        return mirror.getType() == Class.class;
    }

    public boolean supportToJson(Mirror<?> mirror, Object obj, JsonFormat jf) {
        return obj != null && obj instanceof Class;
    }

    @SuppressWarnings("rawtypes")
    public void toJson(Mirror<?> mirror, Object currentObj, JsonRender r, JsonFormat jf) throws IOException {
        r.string2Json(((Class) currentObj).getName());
    }

    public Object fromJson(Object obj, Mirror<?> mirror) throws Exception {
        return Wlang.loadClass(String.valueOf(obj));
    }
}
