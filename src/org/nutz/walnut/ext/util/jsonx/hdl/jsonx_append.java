package org.nutz.walnut.ext.util.jsonx.hdl;

import java.util.Collection;
import java.util.LinkedList;

import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

import com.alibaba.fastjson.JSON;

public class jsonx_append extends JsonXFilter {

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        // 确保上下文是列表
        Collection<Object> col;
        
        if(null == fc.obj) {
            col = new LinkedList<Object>();
            fc.obj = col;
        }
        // 本身就是列表
        else if(fc.obj instanceof Collection<?>) {
            col = (Collection<Object>)fc.obj;
        }
        // 本身需要变成列表
        else {
            col = new LinkedList<Object>();
            col.add(fc.obj);
            fc.obj = col;
        }
        
        // 逐个解析值，并插入集合
        for(String val : params.vals) {
            Object vo = JSON.parse(val);
            if(vo instanceof Collection<?>) {
                for(Object ve:(Collection<?>)vo) {
                    col.add(ve);
                }
            }else {
                col.add(vo);
            }
        }
    }
    

}
