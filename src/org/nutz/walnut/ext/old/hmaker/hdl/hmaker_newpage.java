package org.nutz.walnut.ext.old.hmaker.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.WebException;

public class hmaker_newpage implements JvmHdl {
    
    private String __get_newpage_content(){
        return Files.read("org/nutz/walnut/ext/hmaker/hdl/newpage.html");
    }

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        String pgName = hc.params.vals.length > 0 ? hc.params.vals[0] : "NewPage";

        // 创建一个不同名的
        int retry = 0;
        WnObj oPage = null;
        while (true) {
            String nm = retry > 0 ? String.format("%s(%d)", pgName, retry) : pgName;
            try {
                oPage = sys.io.create(hc.oRefer, nm, WnRace.FILE);
                break;
            }
            catch (WebException e) {
                // 重名了，继续
                if (e.isKey("e.io.obj.exists")) {
                    retry++;
                    continue;
                }
                // 否则抛出错误
                throw e;
            }
        }

        // 修改类型
        oPage.type("html").mime("text/html");
        sys.io.set(oPage, "^(tp|mime)$");
        
        // 写入初始内容
        String html = this.__get_newpage_content();
        sys.io.writeText(oPage, html);

        // 输出新的对象信息
        sys.out.println(Json.toJson(oPage, hc.jfmt));

    }

}
