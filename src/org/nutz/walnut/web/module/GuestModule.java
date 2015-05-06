package org.nutz.walnut.web.module;

import java.io.InputStream;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnAsUsr;

/**
 * 提供未登录访客访问内容的方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/gu")
@Filters(@By(type = WnAsUsr.class, args = {"guest", "guest"}))
public class GuestModule extends AbstractWnModule {

    @At("/**")
    @Fail("http:404")
    public View read(String str) {
        WnObj o = Wn.checkObj(io, str);

        // 返回输入流
        String contentType = o.mime();

        InputStream in = io.getInputStream(o, 0);
        return new RawView2(contentType, in, (int) o.len());
    }

}
