package org.nutz.mvc.adaptor.extractor;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.site0.walnut.util.Wlang;
import org.nutz.mvc.adaptor.ParamExtractor;

/**
 * 默认提取器
 * 
 * @author juqkai(juqkai@gmail.com)
 * 
 */
public class BaseParamExtractor implements ParamExtractor {
    private HttpServletRequest req;

    public BaseParamExtractor(HttpServletRequest req) {
        this.req = req;
    }

    public String[] extractor(String name) {
        if (req == null)
            return new String[0];
        return req.getParameterValues(name);
    }

    public Set<String> keys() {
        if (req == null)
            return new HashSet<String>();
        return (Set<String>) Wlang.enum2collection(req.getParameterNames(), new HashSet<String>());
    }

}
