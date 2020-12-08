package org.nutz.walnut.ext.o.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class o_create extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        WnObj oP;
        // 首先确认一下父目录
        if (params.hasString("p")) {
            oP = Wn.checkObj(sys, params.getString("p"));
        }
        // 采用当前目录
        else {
            oP = sys.getCurrentObj();
        }
        
        // 确保是目录
        if(oP.isFILE()) {
            oP = oP.parent();
        }
        
        // 准备创建里列表
        List<Object> list = new LinkedList<>();
        
        // 看看自己的参数
        
        // 参数神码都没有，就读取标准输入
        if(list.isEmpty()) {
            
        }
        
        // 然后依次创建对象，并加入到上下文
        
    }

}
