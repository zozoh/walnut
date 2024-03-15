package org.nutz.mvc.upload.injector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.mvc.adaptor.ParamInjector;
import org.nutz.mvc.upload.TempFile;

import com.site0.walnut.util.Wlang;

public class TempFileArrayInjector implements ParamInjector {

    public static final TempFile[] EMTRY = new TempFile[0];

    protected String name;

    public TempFileArrayInjector(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public Object get(ServletContext sc,
                      HttpServletRequest req,
                      HttpServletResponse resp,
                      Object refer) {
        if (refer == null)
            return null;
        Object obj = ((Map<String, Object>) refer).get(name);
        if (obj == null || Wlang.eleSize(obj) == 0)
            return EMTRY;
        if (Wlang.eleSize(obj) == 1) {
            Object tmp = Wlang.firstInAny(obj);
            if (tmp == null || !(tmp instanceof TempFile))
                return EMTRY;
            return new TempFile[]{(TempFile) tmp};
        }
        final List<TempFile> list = new ArrayList<TempFile>();
        Wlang.eachEvenMap(obj, (int index, Object ele, Object src) -> {
            if (ele instanceof TempFile) {
                list.add((TempFile) ele);
            }
        });
        if (list.isEmpty())
            return EMTRY;
        return list.toArray(new TempFile[list.size()]);
    }

}
