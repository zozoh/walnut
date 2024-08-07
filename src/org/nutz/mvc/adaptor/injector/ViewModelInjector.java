package org.nutz.mvc.adaptor.injector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.mvc.ViewModel;
import org.nutz.mvc.adaptor.ParamInjector;

public class ViewModelInjector implements ParamInjector {

    public Object get(ServletContext sc,
                      HttpServletRequest req,
                      HttpServletResponse resp,
                      Object refer) {
        return new ViewModel();
    }

}
