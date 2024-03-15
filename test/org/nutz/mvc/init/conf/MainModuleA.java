package org.nutz.mvc.init.conf;

import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

@Fail("json")
public class MainModuleA {

    @At("/param/a")
    @Ok("json")
    public List<String> f_A(@Param("ids") long[] ids) {
        return Wlang.list(Json.toJson(ids));
    }

    @At("/param/b")
    @Ok("raw")
    public String f_B(@Param("ids") long[] ids) {
        return Json.toJson(ids);
    }

}
