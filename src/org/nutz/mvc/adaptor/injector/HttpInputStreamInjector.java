package org.nutz.mvc.adaptor.injector;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.site0.walnut.util.Wlang;
import org.nutz.mvc.adaptor.ParamInjector;

public class HttpInputStreamInjector implements ParamInjector {

    public Object get(ServletContext sc,
                      HttpServletRequest req,
                      HttpServletResponse resp,
                      Object refer) {
        try {
            return req.getInputStream();
        }
        catch (IOException e) {
            throw Wlang.wrapThrow(e);
        }
    }

}
