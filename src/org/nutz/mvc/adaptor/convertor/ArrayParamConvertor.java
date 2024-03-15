package org.nutz.mvc.adaptor.convertor;

import java.lang.reflect.Array;

import org.nutz.mvc.adaptor.ParamConvertor;
import org.nutz.mvc.adaptor.Params;
import com.site0.walnut.util.Wlang;

public class ArrayParamConvertor implements ParamConvertor {

    private Class<?> eleType;

    private ParamConvertor convertor;

    public ArrayParamConvertor(Class<?> eleType) {
        this.eleType = eleType;
        this.convertor = Params.makeParamConvertor(eleType, null);
    }

    public Object convert(String[] ss) {
        if (null == ss)
            return null;

        Object re = Array.newInstance(eleType, ss.length);
        for (int i = 0; i < ss.length; i++) {
            Array.set(re, i, convertor.convert(Wlang.array(ss[i])));
        }
        return re;
    }

}
